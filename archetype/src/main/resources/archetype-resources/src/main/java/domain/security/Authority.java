#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.domain.security;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import org.easyj.rest.validation.groups.POSTChecks;
import org.easyj.rest.validation.groups.PUTChecks;

@Entity
@Table(name = "security_authority")
@NamedQueries({
    @NamedQuery(name = "Authority.findAll", query = "SELECT a FROM Authority a"),
    @NamedQuery(name = "Authority.findById", query = "SELECT a FROM Authority a WHERE a.id = :id"),
    @NamedQuery(name = "Authority.findByAuthority", query = "SELECT a FROM Authority a WHERE a.authority = :authority")})
public class Authority implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Null(groups={POSTChecks.class})
    @NotNull(groups={PUTChecks.class})
    @Min(value=0, groups={PUTChecks.class})
    @Column(name = "id", nullable = false)
    private Short id;
    @NotNull
    @Size(min = 1, max = 45)
    @Column(name = "authority", nullable = false, length = 45)
    private String authority;

    public Authority() {
    }

    public Authority(Short id) {
        this.id = id;
    }

    public Authority(String authority) {
        this.authority = authority;
    }

    public Authority(Short id, String authority) {
        this.id = id;
        this.authority = authority;
    }

    public Short getId() {
        return id;
    }

    public void setId(Short id) {
        this.id = id;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Authority)) {
            return false;
        }
        Authority other = (Authority) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "${package}.domain.security.Authority[ id=" + id + " ]";
    }
    
}
