package es.codeurjc.AcademiaElSoto.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import es.codeurjc.AcademiaElSoto.model.Cart;
import es.codeurjc.AcademiaElSoto.model.Comment;
import es.codeurjc.AcademiaElSoto.model.User;
import es.codeurjc.AcademiaElSoto.repository.CartRepository;
import es.codeurjc.AcademiaElSoto.repository.CommentRepository;
import es.codeurjc.AcademiaElSoto.repository.UserRepository;

@Service
public class AuthorizationService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CommentRepository commentRepository;

    public AuthorizationService(UserRepository userRepository,
                                CartRepository cartRepository,
                                CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.commentRepository = commentRepository;
    }

    public boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                                || authority.getAuthority().equals("ADMIN"));
    }

    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }

        return userRepository.findByUserName(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }

    public void checkUserAccess(Long userId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return;
        }

        User currentUser = getCurrentUser(authentication);

        if (!currentUser.getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access another user's data");
        }
    }

    public void checkCartAccess(Long cartId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return;
        }

        User currentUser = getCurrentUser(authentication);

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        if (cart.getUser() == null || !cart.getUser().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot access another user's cart");
        }
    }

    public void checkCommentAccess(Long commentId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return;
        }

        User currentUser = getCurrentUser(authentication);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (comment.getUser() == null || !comment.getUser().equals(currentUser.getUserName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot modify another user's comment");
        }
    }
}