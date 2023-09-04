package com.example.jump.entity;


import lombok.*;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import java.util.Set;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class ClubMember extends BaseEntity {

    @Id
    private String email;

    private String password;

    private String name;

    private boolean fromSocial;

<<<<<<< HEAD
    private Boolean root;
=======
    private String root;

>>>>>>> 4a1b7f01a5f80c91666df3c4220686c5ea2ae776
    @ElementCollection(fetch = FetchType.LAZY)
    private Set<ClubMemberRole> roleSet;

    public void addMemberRole(ClubMemberRole clubMemberRole){
        roleSet.add(clubMemberRole);
    }

}
