package pk.training.basit.jpa.entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.userdetails.UserDetails;

import pk.training.basit.jpa.audit.AuditDeletedDate;

/**
 * The first two and most basic JPA mapping annotations are @javax.persistence.Entity and @javax.persistence.Table. @Entity marks a 
 * class to indicate that it is an entity. Every entity must have this annotation. By default, an entity name is equal to the 
 * unqualified entity class name, so the following pk.training.basit.entities.Book class has an entity name of Author.
 * 
 * The table name to which an entity maps defaults is the entity name. So the default table name for UserPrincipal is UserPrincipal. 
 * uniqueConstraints is a special attribute used exclusively for schema generation. JPA providers can automatically generate 
 * database schema based off of your entities, and this attribute enables you to indicate that a particular column or columns should
 * form a unique constraint. 
 * As of JPA 2.1, you can also use the indexes attribute to specify indexes that JPA should create when using schema generation.
 */
@Entity
@Table(
	uniqueConstraints = {
        @UniqueConstraint(
    	    name = "UserPrincipal_Username", 
    	    columnNames = "Username"
        )
    }
)
/**
 * When you design your system, ultimately you need to decide whether you want to couple the user object for persistence purposes 
 * with the user object for authentication and authorization purposes. The advantage to separating them is that you really don’t 
 * need (or even want) a mutable user object carried around in the security context, and keeping them separate enforces this mantra. 
 * However, the advantage to making them the same object is that you don't have to do as much work to embed the user entity in other
 * entities. Ultimately, only you can decide the approach that works best for you.
 * 
 * For simplicity, the Customer Support application combines the UserDetails implementation and the entity in the same object. The 
 * changes to UserPrincipal are pretty simple. It now implements UserDetails instead of Authentication and also implements 
 * org.springframework.security.core.CredentialsContainer so that Spring Security clears the password when the authentication process
 * is complete.
 */
public class UserPrincipal implements UserDetails, CredentialsContainer, Cloneable {
	
	private static final long serialVersionUID = 1L;

	/**
	 * One of the first things you must do when mapping JPA entities is create surrogate keys, also called primary keys or IDs, for 
	 * those entities. Every entity should have an ID, which can be one field (and thus a single column) or multiple fields (and thus
	 * multiple columns). As a result, you have many different approaches for mapping an ID.
	 * 
	 * First, you can mark any JavaBean property with @javax.persistence.Id. This annotation can go on the private field or the 
	 * public accessor method and indicates that the property is the entity’s surrogate key. The property may be any Java primitive, 
	 * primitive wrapper, String, java.util.Date, java.sql.Date, java.math.BigInteger, or java.math.BigDecimal.
	 * 
	 * If your entity class does not have an @Id property, the JPA provider looks for a property named id (with accessor getId and 
	 * mutator setId) and uses it automatically. This property must also be a primitive, primitive wrapper, String, java.util.Date, 
	 * java.sql.Date, BigInteger, or BigDecimal.
	 * 
	 * You can, whenever you want, create an entity with an ID that is manually generated and assigned. However, this is rarely 
	 * desired because it requires extra, repetitive, unnecessary work. Typically, you want your entity IDs to be automatically 
	 * generated in some manner. You can accomplish this using the @javax.persistence.GeneratedValue annotation. @GeneratedValue 
	 * enables you to specify a generation strategy and, if necessary, a generator name. For example, the Book and Author entity IDs
	 * use javax.persistence.GenerationType.IDENTITY to indicate that the database column the ID is stored in can generate its own 
	 * value automatically.
	 * 
	 * This is compatible with MySQL AUTO_INCREMENT columns, Microsoft SQL Server and Sybase IDENTITY columns, PostgreSQL SERIAL and 
	 * DEFAULT NEXTVAL() columns, Oracle DEFAULT SYS_GUID() columns, and more. You cannot use GenerationType.IDENTITY with databases 
	 * that do not support the auto-generation of column values, but all the most common relational databases support this.
	 * 
	 * @return
	 */
	@Id
	@Column(name = "UserId")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Basic
	private String username;
	
	/**
	 * You can use @Basic’s optional attribute to indicate that the property may be null. (This is only a hint and is useful for 
	 * schema generation.) This is ignored for primitives, which may never be null.
	 */
	@Basic(fetch = FetchType.LAZY)
    @Column(name = "HashedPassword")
    private String hashedPassword;
	
	private boolean enabled;
	private boolean accountNonExpired;
	private boolean credentialsNonExpired;
	private boolean accountNonLocked;
	
	// @CollectionTable in UserPrincipal maps UserAuthority to the UserPrincipalAuthority table.
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
    	name = "UserPrincipalAuthority", 
    	joinColumns = {
            @JoinColumn(
            	name = "UserId", 
            	referencedColumnName = "UserId"
            )
       }
    )
    private Set<UserAuthority> authorities = new HashSet<>();
    
    @Embedded
    private AuditDeletedDate audit = new AuditDeletedDate();
   
    public UserPrincipal() {
		
	}

    public UserPrincipal(Long id, String username, String password, Set<UserAuthority> authorities) {
		this(id, username, password, true, true, true, true, authorities, null);
	}
    
	public UserPrincipal(Long id, String username, String password, boolean enabled, boolean accountNonExpired, 
			boolean credentialsNonExpired, boolean accountNonLocked, Set<UserAuthority> authorities, AuditDeletedDate audit) {
		this.id = id;
		this.username = username;
		this.hashedPassword = password;
		this.enabled = enabled;
		this.accountNonExpired = accountNonExpired;
		this.credentialsNonExpired = credentialsNonExpired;
		this.accountNonLocked = accountNonLocked;
		this.authorities = Collections.unmodifiableSet(authorities);
		this.audit = audit;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

    public String getHashedPassword() {
        return this.hashedPassword;
    }

    public void setHashedPassword(String password) {
        this.hashedPassword = password;
    }
    
    @Transient
    @Override
    public String getPassword() {
        return this.getHashedPassword() == null ? null : this.getHashedPassword();
    }

    @Override
    public void eraseCredentials() {
        this.hashedPassword = null;
    }
	
    @Override
    public Set<UserAuthority> getAuthorities() {
        return this.authorities;
    }

    public void setAuthorities(Set<UserAuthority> authorities) {
        this.authorities = authorities;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public AuditDeletedDate getAudit() {
		return audit;
	}

	public void setAudit(AuditDeletedDate audit) {
		this.audit = audit;
	}

	@Override
    public int hashCode() {
        return this.username.hashCode();
    }

    @Override
    public boolean equals(Object other) {
    	return other instanceof UserPrincipal && ((UserPrincipal)other).id == this.id;
    }

    @Override
    protected UserPrincipal clone() {
    	try {
            return (UserPrincipal)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e); // not possible
        }
    }

    @Override
    public String toString() {
        return this.username;
    }
    
}
