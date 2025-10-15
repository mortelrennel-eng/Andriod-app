package com.example.finalproject;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersListActivity extends AppCompatActivity {

    private RecyclerView usersRecyclerView;
    private UserSectionAdapter adapter;
    private List<Object> displayList; // Can hold SectionHeader or User objects
    private List<Object> originalList; // To hold the unfiltered list
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("All Users by Section");

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        displayList = new ArrayList<>();
        originalList = new ArrayList<>();
        adapter = new UserSectionAdapter(displayList);
        usersRecyclerView.setAdapter(adapter);

        searchView = findViewById(R.id.searchView);
        setupSearchView();

        loadUsersBySection();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String text) {
        displayList.clear();
        if (text.isEmpty()) {
            displayList.addAll(originalList);
        } else {
            text = text.toLowerCase();
            for (Object item : originalList) {
                if (item instanceof SectionHeader) {
                    // You can decide if you want to add section headers even when filtering
                } else if (item instanceof User) {
                    User user = (User) item;
                    String fullName = (user.getFirstName() + " " + user.getLastName()).toLowerCase();
                    if (fullName.contains(text) || (user.getSection() != null && user.getSection().toLowerCase().contains(text))) {
                        displayList.add(user);
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadUsersBySection() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                originalList.clear();
                Map<String, List<User>> sectionsMap = new HashMap<>();

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        String section = user.getSection() != null ? user.getSection() : "Unassigned";
                        if (!sectionsMap.containsKey(section)) {
                            sectionsMap.put(section, new ArrayList<>());
                        }
                        sectionsMap.get(section).add(user);
                    }
                }

                for (Map.Entry<String, List<User>> entry : sectionsMap.entrySet()) {
                    originalList.add(new SectionHeader(entry.getKey()));
                    originalList.addAll(entry.getValue());
                }
                
                filter(searchView.getQuery().toString());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
