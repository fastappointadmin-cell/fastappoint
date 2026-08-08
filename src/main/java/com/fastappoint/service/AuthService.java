package com.fastappoint.service;

import com.fastappoint.domain.AppUser;
import com.fastappoint.domain.Business;
import com.fastappoint.domain.BusinessMembership;
import com.fastappoint.domain.MembershipRole;
import com.fastappoint.domain.OAuthIdentity;
import com.fastappoint.domain.OAuthProvider;
import com.fastappoint.dto.AuthResponse;
import com.fastappoint.dto.LoginRequest;
import com.fastappoint.dto.MembershipDTO;
import com.fastappoint.dto.RegisterRequest;
import com.fastappoint.dto.UserDTO;
import com.fastappoint.exception.AuthenticationFailedException;
import com.fastappoint.exception.EmailAlreadyInUseException;
import com.fastappoint.repository.AppUserRepository;
import com.fastappoint.repository.BusinessRepository;
import com.fastappoint.repository.OAuthIdentityRepository;
import com.fastappoint.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** Orchestrates registration, login (password + OAuth), and session refresh. All the actual token
 * mechanics live in {@link JwtService} (access) and {@link RefreshTokenService} (refresh); this class
 * is just the business rules around them. */
@Service
public class AuthService {

    private final AppUserRepository users;
    private final BusinessRepository businesses;
    private final BusinessSlugService slugService;
    private final OAuthIdentityRepository oauthIdentities;
    private final MembershipService membershipService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final BusinessPhoneNumberService businessPhoneNumberService;

    public AuthService(AppUserRepository users, BusinessRepository businesses, BusinessSlugService slugService,
                        OAuthIdentityRepository oauthIdentities, MembershipService membershipService,
                        PasswordEncoder passwordEncoder, JwtService jwtService,
                        RefreshTokenService refreshTokenService,
                        BusinessPhoneNumberService businessPhoneNumberService) {
        this.users = users;
        this.businesses = businesses;
        this.slugService = slugService;
        this.oauthIdentities = oauthIdentities;
        this.membershipService = membershipService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.businessPhoneNumberService = businessPhoneNumberService;
    }

    public record IssuedSession(AuthResponse response, String rawRefreshToken, LocalDateTime refreshExpiresAt) {
    }

    @Transactional
    public IssuedSession register(RegisterRequest request) {
        if (request.getBusinessName() == null || request.getBusinessName().isBlank())
            throw new IllegalArgumentException("Business name is required");
        if (request.getEmail() == null || request.getEmail().isBlank())
            throw new IllegalArgumentException("Email is required");
        if (request.getPassword() == null || request.getPassword().length() < 8)
            throw new IllegalArgumentException("Password must be at least 8 characters");

        String email = normalizeEmail(request.getEmail());
        if (users.existsByEmail(email)) {
            throw new EmailAlreadyInUseException("An account with this email already exists");
        }

        String displayName = request.getDisplayName() != null && !request.getDisplayName().isBlank()
                ? request.getDisplayName()
                : request.getEmail();
        AppUser user = users.save(new AppUser(email, passwordEncoder.encode(request.getPassword()), displayName));

        String businessName = request.getBusinessName().trim();
        Business business = businesses.save(new Business(
                businessName,
                slugService.generateUniqueSlug(businessName),
                businessPhoneNumberService.generateUniqueDefaultNumber(),
                ""));
        membershipService.grant(user, business, MembershipRole.OWNER);

        return issueSession(user);
    }

    @Transactional
    public IssuedSession login(LoginRequest request) {
        AppUser user = users.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(() -> new AuthenticationFailedException("Invalid email or password"));
        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid email or password");
        }
        return issueSession(user);
    }

    /** Resolve-or-create an AppUser from an OAuth provider identity, then issue a session the same way
     * as password login. A brand-new user (no memberships yet) lands with an empty memberships list --
     * the frontend routes them to the "name your business" completion step in that case. */
    @Transactional
    public IssuedSession loginWithOAuth(OAuthProvider provider, String providerUserId, String email, String displayName) {
        AppUser user = oauthIdentities.findByProviderAndProviderUserId(provider, providerUserId)
                .map(OAuthIdentity::getUser)
                .orElseGet(() -> {
                    AppUser resolved = users.findByEmail(normalizeEmail(email))
                            .orElseGet(() -> users.save(new AppUser(
                                    normalizeEmail(email), null, displayName != null ? displayName : email)));
                    oauthIdentities.save(new OAuthIdentity(resolved, provider, providerUserId));
                    return resolved;
                });
        return issueSession(user);
    }

    @Transactional
    public IssuedSession refresh(String rawRefreshToken) {
        RefreshTokenService.IssuedToken rotated = refreshTokenService.rotate(rawRefreshToken);
        return new IssuedSession(buildResponse(rotated.user()), rotated.rawValue(), rotated.expiresAt());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    @Transactional(readOnly = true)
    public AuthResponse me(UUID userId) {
        AppUser user = users.findById(userId)
                .orElseThrow(() -> new AuthenticationFailedException("User not found"));
        return buildResponse(user);
    }

    private IssuedSession issueSession(AppUser user) {
        RefreshTokenService.IssuedToken refreshToken = refreshTokenService.issue(user);
        return new IssuedSession(buildResponse(user), refreshToken.rawValue(), refreshToken.expiresAt());
    }

    private AuthResponse buildResponse(AppUser user) {
        String accessToken = jwtService.issueAccessToken(user);
        List<MembershipDTO> memberships = membershipService.findByUser(user.getId()).stream()
                .map(this::toMembershipDTO)
                .collect(Collectors.toList());
        return new AuthResponse(accessToken, new UserDTO(user.getId(), user.getEmail(), user.getDisplayName()), memberships);
    }

    private MembershipDTO toMembershipDTO(BusinessMembership membership) {
        return new MembershipDTO(
                membership.getBusiness().getId(),
                membership.getBusiness().getName(),
                membership.getRole().name());
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
