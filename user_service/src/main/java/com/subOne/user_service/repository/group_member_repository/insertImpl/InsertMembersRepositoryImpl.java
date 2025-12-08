package com.subOne.user_service.repository.group_member_repository.insertImpl;

import com.subOne.user_service.repository.group_member_repository.insert.InsertMembersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class InsertMembersRepositoryImpl implements InsertMembersRepository {
    private final DatabaseClient databaseClient;

    @Override
    public Mono<Void> insertAllMembers(List<Integer> groupsId, UUID userId) {
        StringBuilder query = new StringBuilder(
                """
                insert into group_members (group_id, user_id) values
                """);
        for (int i = 0; i < groupsId.size(); i++) {
            query.append("(:group_id").append(i);
            query.append(", :user_id").append("),");
        }
        query.deleteCharAt(query.length() - 1);
        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(query.toString());
        for (int i = 0; i < groupsId.size(); i++) {
            spec = spec.bind("group_id" + i, groupsId.get(i));
            spec = spec.bind("user_id", userId);
        }
        return spec.then();
    }
}
