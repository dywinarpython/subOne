package com.subOne.subscriptions_service.repository.analytic_subscription_repository.insertImpl;

import com.subOne.subscriptions_service.entity.AnalyticSubscription;
import com.subOne.subscriptions_service.repository.analytic_subscription_repository.insert.InsertAnalyticSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class InsertAnalyticSubscriptionRepositoryImpl implements InsertAnalyticSubscriptionRepository {
    private final DatabaseClient databaseClient;

    @Override
    public Mono<Void> insertAllAnalyticSubscription(List<AnalyticSubscription> analyticSubscriptions) {
        StringBuilder query = new StringBuilder(
                """
                insert into analytic_subscriptions (subscription_id, date_paid, amount) values
                """);
        for (int i = 0; i < analyticSubscriptions.size(); i++) {
            query.append("(:subscriptionId").append(i);
            query.append(", :datePaid").append(i);
            query.append(", :amount").append(i).append("),");
        }
        query.deleteCharAt(query.length() -1);
        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(query.toString());
        for (int i = 0; i < analyticSubscriptions.size(); i++) {
            spec = spec.bind("subscriptionId" + i, analyticSubscriptions.get(i).getSubscriptionId());
            spec = spec.bind("datePaid" + i, analyticSubscriptions.get(i).getDatePaid());
            spec = spec.bind("amount" + i, analyticSubscriptions.get(i).getAmount());
        }
        return spec.then();
    }
}
