package com.gitgle.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class HotDomainEventResponse implements Serializable {

    private List<HotDomainUser> hotDomainUserList;

    private List<HotDomainRepo> hotDomainRepoList;

    private List<HotDomainOrganization> hotDomainOrganizationList;
}
