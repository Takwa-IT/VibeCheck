package com.example.vibecheck;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.vibecheck.fragments.BubblePopFragment;
import com.example.vibecheck.fragments.QuizFragment;
import com.example.vibecheck.fragments.BreathingFragment;
import com.example.vibecheck.fragments.MemoryFragment;
import java.util.List;

public class GamePagerAdapter extends FragmentStateAdapter {
    private final List<String> games;

    public GamePagerAdapter(@NonNull List<String> games, AmusementActivity activity) {
        super(activity);
        this.games = games;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String gameTitle = games.get(position);

        if (gameTitle.contains("Bubble Pop")) {
            return new BubblePopFragment();
        } else if (gameTitle.contains("Quiz")) {
            return new QuizFragment();

        } else if (gameTitle.contains("Respiration")) {
            return new BreathingFragment();
        } else if (gameTitle.contains("Mémoire")) {
            return new MemoryFragment();
        } else {
            return new MemoryFragment(); // Fallback
        }
    }

    @Override
    public int getItemCount() {
        return games.size();
    }
}