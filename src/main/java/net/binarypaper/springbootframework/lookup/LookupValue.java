/*
 * Copyright 2016 <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.binarypaper.springbootframework.lookup;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import net.binarypaper.springbootframework.entity.AuditRevision;
import net.binarypaper.springbootframework.entity.DatedEntity;
import net.binarypaper.springbootframework.entity.Updatable;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.QueryHints;

/**
 * A lookup value in a lookup list on screen
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// JPA annotations
@Entity
@Table(name = "LOOKUP_VALUE", uniqueConstraints = {
    @UniqueConstraint(name = "UC_LOOKUP_LIST_VALUE", columnNames = {"LOOKUP_LIST_NAME", "DISPLAY_VALUE", "PARENT"})
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "LookupValueRegion")
@NamedQueries({
    @NamedQuery(
            name = "LookupValue.findByLookupListName",
            query = "SELECT lv FROM LookupValue lv WHERE lv.lookupListName = :lookupListName ORDER BY lv.displayValue",
            hints = {
                @QueryHint(name = QueryHints.CACHEABLE, value = "true")
                ,@QueryHint(name = QueryHints.CACHE_REGION, value = "QueriesRegion")
            }
    )
    ,@NamedQuery(
            name = "LookupValue.findByLookupListNameAndParentId",
            query = "SELECT lv FROM LookupValue lv WHERE lv.lookupListName = :lookupListName AND lv.parent.id = :parentId ORDER BY lv.displayValue",
            hints = {
                @QueryHint(name = QueryHints.CACHEABLE, value = "true")
                ,@QueryHint(name = QueryHints.CACHE_REGION, value = "QueriesRegion")
            }
    )
    ,@NamedQuery(
            name = "LookupValue.findByLookupListNameAndDisplayValue",
            query = "SELECT lv FROM LookupValue lv WHERE lv.lookupListName = :lookupListName AND lv.displayValue = :displayValue",
            hints = {
                @QueryHint(name = QueryHints.CACHEABLE, value = "true")
                ,@QueryHint(name = QueryHints.CACHE_REGION, value = "QueriesRegion")
            }
    )
})
// Envers annotations
@Audited
// Jackson annotations
@JsonPropertyOrder({
    "id",
    "version",
    "active",
    "effectiveFrom",
    "effectiveTo",
    "lookupListName",
    "displayValue",
    "parentId",
    "revision"
})
// Lombok annotations
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"parent", "children", "revision"})
@ToString(callSuper = true, exclude = {"parent", "children", "revision"})
public class LookupValue extends DatedEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    // JPA annotations
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOOKUP_VALUE_ID")
    // Jackson annotations
    @JsonView({
        View.List.class,
        View.All.class,
        View.Edit.class,
        AuditRevision.class
    })
    // Swagger annotations
    @ApiModelProperty(
            value = "The unique ID of the lookup value. "
            + "The id must not be specified when adding a new lookup value.",
            example = "1",
            readOnly = true,
            position = 1
    )
    private Long id;

    // JPA annotations
    @Version
    // Jackson annotations
    @JsonView({
        View.All.class,
        View.Edit.class
    })
    // Swagger annotations
    @ApiModelProperty(
            value = "The version of the database record used for concurrency control. "
            + "The version must not be specified when adding a new lookup value.",
            example = "1",
            readOnly = true,
            position = 2
    )
    private Long version;

    // JPA annotations
    @Column(name = "LOOKUP_LIST_NAME", updatable = false)
    // Bean validation annotations
    @NotNull(message = "{LookupValue.lookupListName.NotNull}")
    @Size(min = 3, max = 100, message = "{LookupValue.lookupListName.Size}")
    @Pattern(regexp = "[a-z,A-Z,0-9,-]*", message = "{LookupValue.lookupListName.Pattern}")
    // Jackson annotations
    @JsonProperty(value = "lookup-list")
    @JsonView({
        View.List.class,
        View.All.class,
        View.Add.class,
        View.Edit.class,
        AuditRevision.class
    })
    // Swagger annotations
    @ApiModelProperty(
            value = "The lookup list name of the lookup value",
            example = "vehicle-make",
            required = true,
            position = 3
    )
    private String lookupListName;

    // JPA annotations
    @Column(name = "DISPLAY_VALUE")
    // Framework annotations
    @Updatable
    // Bean Validation annotations
    @NotNull(message = "{LookupValue.displayValue.NotNull}")
    // Jackson annotations
    @JsonProperty(value = "display-value")
    @JsonView({
        View.List.class,
        View.All.class,
        View.Add.class,
        View.Edit.class,
        AuditRevision.class
    })
    // Swagger annotations
    @ApiModelProperty(
            value = "The display value of the lookup value",
            example = "Ford",
            required = true,
            position = 4
    )
    private String displayValue;

    // JPA annotations
    @ManyToOne(optional = true)
    @JoinColumn(name = "PARENT", foreignKey = @ForeignKey(name = "FK_LOOKUP_VALUE_PARENT"))
    // Bean Validation annotations
    @Valid
    // Jackson annotations
    @JsonIgnore
    private LookupValue parent;

    // JPA annotations
    @Transient
    // Jackson annotations
    @JsonProperty(value = "parent-id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonView({
        View.List.class,
        View.All.class,
        View.Add.class,
        AuditRevision.class
    })
    // Swagger annotations
    @ApiModelProperty(
            value = "The id of the parent lookup value",
            example = "1",
            position = 5
    )
    // Lombok annotations
    @Getter(AccessLevel.NONE)
    private Long parentId;

    // JPA annotations
    @OneToMany(mappedBy = "parent")
    // Envers annotations
    @NotAudited
    // Bean Validation annotations
    @Valid
    // Jackson annotations
    @JsonIgnore
    private List<LookupValue> children = new ArrayList<>();

    // JPA Annotations
    @Transient
    // Jackson annotations
    @JsonView({
        AuditRevision.class
    })
    // Swagger annotations
    @ApiModelProperty(
            value = "The the audit revision details of the lookup value",
            readOnly = true,
            position = 6
    )
    private AuditRevision revision;

    public Long getParentId() {
        if (parentId == null && parent != null) {
            parentId = parent.getId();
        }
        return parentId;
    }

    public void addChild(LookupValue child) {
        child.setParent(this);
        children.add(child);
    }

    public void removeChild(LookupValue child) {
        child.setParent(null);
        children.remove(child);
    }

    public interface View {

        public interface List {
        }

        public interface All {
        }

        public interface Add {
        }

        public interface Edit {
        }
    }
}
