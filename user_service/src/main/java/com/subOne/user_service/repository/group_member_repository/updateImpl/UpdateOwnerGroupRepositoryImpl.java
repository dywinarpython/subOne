package com.subOne.user_service.repository.group_member_repository.updateImpl;

import com.subOne.user_service.dto.user.request.RequestGroupOwnershipChangesDto;
import com.subOne.user_service.repository.group_member_repository.update.UpdateOwnerGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
@Repository
@RequiredArgsConstructor
public class UpdateOwnerGroupRepositoryImpl implements UpdateOwnerGroupRepository {
    private final DatabaseClient databaseClient;
    @Override
    public Mono<Void> updateOwnerGroup(List<RequestGroupOwnershipChangesDto> requestGroupsOwnershipChangesDtos) {
        StringBuilder query = new StringBuilder("""
                UPDATE groups
                SET owner_id = CASE
                """);
        for (int i = 0; i < requestGroupsOwnershipChangesDtos.size(); i++) {
            query.append(" WHEN ").append("id = ").append(":groupId_").append(i).append(" THEN ").append(":newOwnerId").append(i);
        }
        query.append(" END ");
        query.append("WHERE id IN (");
        List<Integer> groupsId = requestGroupsOwnershipChangesDtos.stream().map(RequestGroupOwnershipChangesDto::groupId).toList();
        for (int i = 0; i < groupsId.size(); i++) {
            query.append(":groupId").append(i).append(",");
        }
        query.deleteCharAt(query.length() -1);
        query.append(")");
        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(query.toString());
        for (int i = 0; i < requestGroupsOwnershipChangesDtos.size(); i++) {
                spec = spec.bind("groupId_" + i, groupsId.get(i));
                spec = spec.bind("newOwnerId" + i, requestGroupsOwnershipChangesDtos.get(i).userId());
                spec = spec.bind("groupId" + i, groupsId.get(i));
        }
        return spec.then();
    }
}
