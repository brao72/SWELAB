package com.libratrack.factory;

import com.libratrack.model.Faculty;
import com.libratrack.model.Member;
import com.libratrack.model.MemberType;
import com.libratrack.model.Student;

public class MemberFactory {

    public static Member createMember(MemberType type, String name, String email, String phone, String passwordHash) {
        return switch (type) {
            case STUDENT -> new Student(name, email, phone, passwordHash);
            case FACULTY -> new Faculty(name, email, phone, passwordHash);
        };
    }
}
