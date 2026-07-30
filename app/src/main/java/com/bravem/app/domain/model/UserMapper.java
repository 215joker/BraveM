package com.bravem.app.domain.model;

public class UserMapper {
    public static User toDomain(com.bravem.app.model.User dataUser) {
        if (dataUser == null) return null;
        return new User(
            dataUser.getUid(),
            dataUser.getFullName(),
            dataUser.getEmail(),
            dataUser.getUniversity(),
            dataUser.getDegreeId(),
            dataUser.getDegreeName(),
            dataUser.getIntake(),
            dataUser.getRole(),
            dataUser.getProfilePicture(),
            dataUser.isSuspended(),
            dataUser.getDeletionRequestedAt()
        );
    }
}
