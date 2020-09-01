package com.mex.whytter.service;

import com.mex.whytter.domain.User;
import com.mex.whytter.domain.UserSubscription;
import com.mex.whytter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileService {
    private final UserRepository userRepository;

    @Autowired
    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User changeSubscription(User channel, User subscriber) {
        List<UserSubscription> subscriptions = channel.getSubscribers()
                .stream()
                .filter(subscripton -> subscripton.getSubscriber().equals(subscriber))
                .collect(Collectors.toList());


        if (subscriptions.isEmpty()) {
            UserSubscription subscription = new UserSubscription(channel, subscriber);
            channel.getSubscribers().add(subscription);
        } else {
            channel.getSubscribers().removeAll(subscriptions);
        }

        return userRepository.save(channel);
    }
}
