package com.gitgle.service;

import com.gitgle.dao.Domain;

import java.util.List;

public interface DomainService {

    List<Domain> readAllDomain();

    Integer getDomainId(String domain);
}
