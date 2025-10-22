package com.subOne.user_service.repository.group_member_repository.deleteImpl;

import com.subOne.user_service.repository.group_member_repository.delete.DeleteAllByPairsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class DeleteAllByPairsRepositoryImpl implements DeleteAllByPairsRepository {
    private final DatabaseClient databaseClient;
    @Override
    public Mono<Void> deleteAllByUserGroupPairs(List<Tuple2<UUID, Long>> pairs) {
        StringBuilder query = new StringBuilder("DELETE FROM group_members WHERE (user_id, group_id) IN (");
        for (int i = 0; i < pairs.size(); i++) {
            query.append("(").append(":userId").append(i).append(" , ").append(":groupId").append(i).append(")").append(",");
        }
        query.deleteCharAt(query.length() -1);
        query.append(")");
        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(query.toString());
        for (int i = 0; i < pairs.size(); i++) {
            Tuple2<UUID, Long> tuple2 = pairs.get(i);
            spec = spec.bind("userId" + i, tuple2.getT1());
            spec = spec.bind("groupId" + i, tuple2.getT2());
        }
        return spec.then();
    }
}
