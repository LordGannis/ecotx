/*******************************************************************************
 * Copyright (C) 2017 http://bndy.net
 * Created by Bendy (Bing Zhang)
 ******************************************************************************/
package net.bndy.wf.modules.core.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.bndy.wf.lib._BaseEntity;

@Entity
@Table(name = "core_user")
public class User extends _BaseEntity implements UserDetails {
    private static final long serialVersionUID = 1L;

    private String username;
    private String avatar;
    private String password;
    private boolean enabled;
    private boolean isExpired;
    private boolean isLocked;
    private boolean isCredentialsExpired;
    private boolean isSuperAdmin;

    @Transient
    @JsonIgnore
    private String passwordConfirm;

    @ManyToMany(fetch = FetchType.EAGER)    // FetchType.EAGER: don't use lazy load.
    @JoinTable(name = "core_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile userProfile;

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public User() {
    }

    public User(User user) {
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.enabled = user.isEnabled();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean isExpired) {
        this.isExpired = isExpired;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public void setCredentialsExpired(boolean isCredentialsExpired) {
        this.isCredentialsExpired = isCredentialsExpired;
    }

    public List<String> getRoleNames() {
        List<String> list = new ArrayList<>();
        if (this.getRoles() != null) {
            for (Role role : this.getRoles()) {
                list.add(role.getName());
            }
        }
        return list;
    }

    @Override
    public Set<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
        Set<Role> roles = this.getRoles();
        if (roles != null) {
            for (Role role : this.getRoles()) {
                grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
            }
        }
        return grantedAuthorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !this.isExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.isLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !this.isCredentialsExpired;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        isSuperAdmin = superAdmin;
    }


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((avatar == null) ? 0 : avatar.hashCode());
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result + (isCredentialsExpired ? 1231 : 1237);
		result = prime * result + (isExpired ? 1231 : 1237);
		result = prime * result + (isLocked ? 1231 : 1237);
		result = prime * result + (isSuperAdmin ? 1231 : 1237);
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((passwordConfirm == null) ? 0 : passwordConfirm.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (avatar == null) {
			if (other.avatar != null)
				return false;
		} else if (!avatar.equals(other.avatar))
			return false;
		if (enabled != other.enabled)
			return false;
		if (isCredentialsExpired != other.isCredentialsExpired)
			return false;
		if (isExpired != other.isExpired)
			return false;
		if (isLocked != other.isLocked)
			return false;
		if (isSuperAdmin != other.isSuperAdmin)
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (passwordConfirm == null) {
			if (other.passwordConfirm != null)
				return false;
		} else if (!passwordConfirm.equals(other.passwordConfirm))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
    
}
