package com.subOne.user_service.repository.group_repository.selectImpl;

import com.subOne.user_service.dto.user.response.ResponseUserDto;
import com.subOne.user_service.repository.group_repository.select.SelectOwnerGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SelectOwnerGroupRepositoryImpl implements SelectOwnerGroupRepository {
    private final DatabaseClient databaseClient;

    @Override
    public Mono<ResponseUserDto> selectOwnerByGroupId(Integer groupId) {
        return databaseClient.sql("""
                select owner_id, u.name, u.surname, u.email
                from groups g
                join users u on u.user_id = g.owner_id
                where g.id = :groupId""")
                .bind("groupId", groupId)
                .map((row, metadata) ->
                    new ResponseUserDto(
                            row.get("owner_id", UUID.class),
                            row.get("name", String.class),
                            row.get("surname", String.class),
                            row.get("email", String.class)
                    )
                ).one();
    }
}
