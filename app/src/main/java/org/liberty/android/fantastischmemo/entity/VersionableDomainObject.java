package org.liberty.android.fantastischmemo.entity;

import java.util.Date;

/**
 * A domain object that has creationDate and updateDate
 */
public interface VersionableDomainObject {
    Date getCreationDate();

    void setCreationDate(Date creationDate);

    Date getUpdateDate();

    void setUpdateDate(Date updateDate);
}
