package com.mex.whytter.repository;

import com.mex.whytter.domain.User;
import com.mex.whytter.domain.UserSubscription;
import com.mex.whytter.domain.UserSubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UserSubscriptionId> {
    List<UserSubscription> findBySubscriber(User user);
}
