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
package net.binarypaper.springbootframework.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.AssertTrue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;
import net.binarypaper.springbootframework.exception.BusinessLogicException;

/**
 * Abstract class that may be extended to make an entity class active in a date
 * range between the effective from and effective to dates.
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// JPA annotations
@MappedSuperclass
// Envers annotations
@Audited
// Lombok annotations
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class DatedEntity extends ActivatableEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    // JPA annotations
    @Column(name = "EFFECTIVE_FROM")
    @Temporal(TemporalType.DATE)
    // Framework annotations
    @Updatable
    // Jackson annotations
    @JsonProperty("effective-from")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+02:00")
    // Swagger annotations
    @ApiModelProperty(
            value = "The effective from date of the entity. "
            + "If not specified the entity is valid from the beginning of time.",
            example = "2016-01-01"
    )
    private Date effectiveFrom;

    // JPA annotations
    @Column(name = "EFFECTIVE_TO")
    @Temporal(TemporalType.DATE)
    // Framework annotations
    @Updatable
    // Jackson annotations
    @JsonProperty("effective-to")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "GMT+02:00")
    // Swagger annotations
    @ApiModelProperty(
            value = "The effective to date of the entity. "
            + "If not specified the entity is valid until the end of time.",
            example = "2016-12-31"
    )
    private Date effectiveTo;

    // Bean validation annotations
    @AssertTrue(message = "{DatedEntity.EffectiveDatesValid}")
    // Jackson annotations
    @JsonIgnore
    public boolean getEffectiveDatesValid() {
        if ((effectiveFrom != null) && (effectiveTo != null)) {
            if (effectiveFrom.after(effectiveTo)) {
                return false;
            }
        }
        return true;
    }

    public static Date parseDate(String dateString, String errorCode) throws BusinessLogicException {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(dateString);
        } catch (ParseException ex) {
            throw new BusinessLogicException(errorCode);
        }
    }

    public static <T extends DatedEntity> List<T> filterByEffectiveDate(List<T> inputList, Date effectiveDate) throws BusinessLogicException {
        // Remove the time from the effectiveDate
        LocalDate localDate = effectiveDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        effectiveDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<T> outputList = new ArrayList<>();
        for (T t : inputList) {
            Date effectiveFrom = t.getEffectiveFrom();
            Date effectiveTo = t.getEffectiveTo();
            if ((effectiveFrom != null) && (effectiveDate.before(effectiveFrom))) {
                continue;
            }
            if ((effectiveTo != null) && (effectiveDate.after(effectiveTo))) {
                continue;
            }
            outputList.add(t);
        }
        return outputList;
    }
}
