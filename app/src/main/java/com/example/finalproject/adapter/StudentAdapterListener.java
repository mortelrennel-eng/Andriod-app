package com.example.finalproject.adapter;

import com.example.finalproject.model.User;

// This interface acts as a contract for any activity using StudentAdapter
public interface StudentAdapterListener {
    void editStudent(User student);
    void deleteStudent(User student);
}
