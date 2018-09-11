/*******************************************************************************
 * Copyright (C) 2017 http://bndy.net
 * Created by Bendy (Bing Zhang)
 ******************************************************************************/
package net.bndy.wf.modules.core.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.bndy.lib.wrapper.LongsWrapper;
import net.bndy.wf.ApplicationContext;
import net.bndy.wf.exceptions.AppException;
import net.bndy.wf.exceptions.DisabledFeatureException;
import net.bndy.wf.lib._BaseApi;
import net.bndy.wf.modules.core.models.File;
import net.bndy.wf.modules.core.models.User;
import net.bndy.wf.modules.core.models.UserProfile;
import net.bndy.wf.modules.core.services.UserService;


@Api(value = "User API")
@RestController
@RequestMapping({"/api/core/users", "/api/v1/core/users"})
public class UserController extends _BaseApi<User> {

    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @ApiOperation(value = "Gets current user")
    @RequestMapping(value = "/me", method = RequestMethod.GET)
    public HashMap<String, Object> me(HttpServletRequest request) throws MalformedURLException {
        User u = getCurrentUser();
        if (u != null) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", u.getUsername());
            map.put("avatar", u.getAvatar());
            map.put("roles", u.getRoles());
            return map;
        }
        return null;
    }
    
    @ApiOperation(value = "Check whether username exists")
    @RequestMapping(value = "/exist", method = RequestMethod.GET)
    public boolean existsUsername(@RequestParam(name = "username") String username) {
    	return this.userService.findByUsername(username, null) != null;
    }

    @ApiOperation(value = "Login")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public boolean login(@RequestBody User user, HttpServletRequest request) {
        User u = this.userService.login(user.getUsername(), user.getPassword());
        if (u != null) {
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                user.getUsername(), user.getPassword());

            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(authRequest);
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);

            // Create a new session and add the security context.
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
            return true;
        }
        return false;
    }

    @ApiOperation(value = "Logout")
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public void logout(HttpServletRequest request) throws ServletException {
        request.logout();
    }

    @ApiOperation(value = "Search by keywords")
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public Page<User> search(
        @RequestParam(name = "keywords", required = false) String keywords,
        @PageableDefault(value = 10, sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) {

        return this.userService.searchUsers(keywords, pageable);
    }

    @ApiOperation(value = "Enable or disable user")
    @RequestMapping(value = "/{userId}/toggleEnabled", method = RequestMethod.PUT)
    public void toggleEnabled(@PathVariable(name = "userId") Long userId) {
        User user = this.userService.get(userId);
        if (user != null) {
            user.setEnabled(!user.isEnabled());
            this.userService.save(user);
        }
    }

    @ApiOperation(value = "Change role for user")
    @RequestMapping(value = "/{userId}/changeRole", method = RequestMethod.PUT)
    public void changeRole(@PathVariable(name = "userId") Long userId, @RequestBody LongsWrapper roleIds) {
        this.userService.changeRole(userId, roleIds.getValues());
    }

    @Override
    public Page<User> get(@PageableDefault(value = 10, sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        return super.get(pageable);
    }

    @Override
    public User post(@RequestBody User entity) {
        return this.userService.register(entity);
    }

    @Override
    public User put(Long id, @RequestBody User entity) {
        return this.userService.register(entity);
    }

    @Override
    @RequestMapping(value = "/updateAvatar", method = RequestMethod.POST, headers = ("content-type=multipart/*"), consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public File upload(MultipartFile file, HttpServletRequest request) throws IllegalStateException, IOException, DisabledFeatureException {
        File f = super.upload(file, request);
        this.userService.updateAvatar(ApplicationContext.getCurrentUser().getId(), f);
        return f;
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    public void changePassword(@RequestBody Map<String, String> data) throws AppException {
        User u = this.getCurrentUser();
        if (this.userService.comparePassword(data.get("oldPassword"), u.getPassword())) {
            this.userService.changePassword(u.getId(), data.get("newPassword"));
        } else {
            throw new AppException("admin.modules.core.userProfile.incorrectOldPassword");
        }
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public UserProfile getUserProfile() {
        return this.userService.getUserProfile(getCurrentUser().getId());
    }
}
