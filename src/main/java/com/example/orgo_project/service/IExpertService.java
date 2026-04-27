package com.example.orgo_project.service;

import com.example.orgo_project.entity.Account;
import com.example.orgo_project.entity.Expert;
import com.example.orgo_project.entity.Seller;

import java.util.List;

public interface IExpertService {
    void register(Expert expert);
    List<Expert> getPendingList();
    Expert findById(int id);
    void approve(int id);
    void reject(int id);
    boolean hasApplied(Account account);
}
