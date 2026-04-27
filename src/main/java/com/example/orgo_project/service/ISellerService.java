package com.example.orgo_project.service;

import com.example.orgo_project.entity.Account;
import com.example.orgo_project.entity.Seller;

import java.util.List;

public interface ISellerService {
    void register(Seller seller);
    List<Seller> getPendingList();
    Seller findById(int id);
    void approve(int id);
    void reject(int id);
    boolean hasApplied(Account account);
}
