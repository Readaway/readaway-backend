package com.revature.readawaybackend.service;

import com.revature.readawaybackend.dao.GiveawayRepository;
import com.revature.readawaybackend.models.Giveaway;
import com.revature.readawaybackend.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;
import java.util.*;

@Service
public class GiveawayService {

    @Autowired
    GiveawayRepository giveawayRepo;

    public Giveaway getGiveawayById(String giveawayId) {
        int id = validateId(giveawayId);
        Optional<Giveaway> optional = giveawayRepo.findById(id);
        if (!optional.isPresent()) throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        return optional.get();
    }

    public Set<Giveaway> getAllGiveawaysByCreatorId(String creatorId) {
        int id = validateId(creatorId);
        return giveawayRepo.findByCreatorId(id);
    }

    public Set<Giveaway> getAllGiveawaysByWinnerId(String winnerId) {
        int id = validateId(winnerId);
        return giveawayRepo.findByWinnerId(id);
    }

    public Set<Giveaway> getAllGiveawaysInProgress() {
        return giveawayRepo.findByWinnerIsNull();
    }

    public void addNewGiveaway(Giveaway giveaway) {
        String isbn = sanitizeIsbn(giveaway.getIsbn());
        giveaway.setIsbn(isbn);
        giveawayRepo.save(giveaway);
        new Timer().schedule(new endGiveawayTask(giveaway.getId()), giveaway.getEndTime());
    }
    // Generate random winner
    private void pickRandomWinner(int giveawayId) {
        // Call update giveawayWinner when selected
        Giveaway giveaway = giveawayRepo.findById(giveawayId).get();
        Set<User> entries = giveaway.getEntrants();
        if (entries.size() == 0) {
            giveawayRepo.delete(giveaway);
        } else {
            User winner = entries.stream().skip(new Random().nextInt(entries.size())).findFirst().get();
            giveaway.setWinner(winner);
            giveawayRepo.save(giveaway);
        }
    }

    private String sanitizeIsbn(String isbn) {
        return isbn.trim();
    }

    private int validateId(String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Illegal Argument provided for path. Path parameter must be an integer.");
        }
    }

    public void serverRestartScheduleGiveaways() {
        for(Giveaway giveaway: getAllGiveawaysInProgress()) {
            if(giveaway.getEndTime().before(Date.from(Instant.now())))
                pickRandomWinner(giveaway.getId());
            else
                new Timer().schedule(new endGiveawayTask(giveaway.getId()), giveaway.getEndTime());
        }
    }

    private class endGiveawayTask extends TimerTask {
        private int giveawayId;

        public endGiveawayTask(int giveawayId) {
            this.giveawayId = giveawayId;
        }

        @Override
        public void run() {
            pickRandomWinner(giveawayId);
        }
    }



}
