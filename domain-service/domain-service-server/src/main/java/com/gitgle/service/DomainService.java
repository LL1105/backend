package com.gitgle.service;

import com.gitgle.dao.Domain;
import com.gitgle.response.UserDomainBase;

import java.util.List;

public interface DomainService {

    List<Domain> readAllDomain();

}
