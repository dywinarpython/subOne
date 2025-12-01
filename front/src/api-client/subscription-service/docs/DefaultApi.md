# DefaultApi

All URIs are relative to *http://localhost:8001*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**deleteSubscription**](#deletesubscription) | **DELETE** /api/v1/groups/{groupId}/subscriptions/{subscriptionId} | Удаление подписки для определенной группы|
|[**extendSubscription**](#extendsubscription) | **PATCH** /api/v1/groups/{groupId}/subscriptions/{subscriptionId}/extend/{extensionCount} | Продления подписки по числу (число &#x3D; значение из PaymentPeriod)|
|[**getAlreadyPaidGroup**](#getalreadypaidgroup) | **GET** /api/v1/groups/{groupId}/subscriptions/analytic/group-summary | Получение аналитических данных группы (контрольная сумма, примерная -&gt; оплата в месяц, оплата в год)|
|[**getAnalyticSubscriptionById**](#getanalyticsubscriptionbyid) | **GET** /api/v1/groups/{groupId}/subscriptions/{subscriptionId}/analytic | Получение аналитических данных подписки|
|[**getPaymentInfoSubscriptionById**](#getpaymentinfosubscriptionbyid) | **GET** /api/v1/groups/{groupId}/subscriptions/{subscriptionId}/payment-info | Получение данных оплаты|
|[**getSubscriptionById**](#getsubscriptionbyid) | **GET** /api/v1/groups/{groupId}/subscriptions/{subscriptionId} | Получение подписки по id для определенной группы|
|[**getSubscriptionsGroup**](#getsubscriptionsgroup) | **GET** /api/v1/groups/{groupId}/subscriptions | Получение всех подписок для определенной группы|
|[**getTotalAnalyticGroups**](#gettotalanalyticgroups) | **GET** /api/v1/groups/me/analytic | Получение аналитики всех групп пользователя (общая оплата, количество подписок)|
|[**saveSubscription**](#savesubscription) | **POST** /api/v1/groups/{groupId}/subscriptions | Создание подписки для определенной группы|
|[**updateSubscription**](#updatesubscription) | **PATCH** /api/v1/groups/{groupId}/subscriptions/{subscriptionId} | Изменение подписки для определенной группы|

# **deleteSubscription**
> deleteSubscription()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let groupId: number; // (default to undefined)
let subscriptionId: number; // (default to undefined)

const { status, data } = await apiInstance.deleteSubscription(
    groupId,
    subscriptionId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **groupId** | [**number**] |  | defaults to undefined|
| **subscriptionId** | [**number**] |  | defaults to undefined|


### Return type

void (empty response body)

### Authorization

[oauth2](../README.md#oauth2)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**204** | No Content |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **extendSubscription**
> string extendSubscription()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let groupId: number; // (default to undefined)
let subscriptionId: number; // (default to undefined)
let extensionCount: number; // (default to undefined)

const { status, data } = await apiInstance.extendSubscription(
    groupId,
    subscriptionId,
    extensionCount
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **groupId** | [**number**] |  | defaults to undefined|
| **subscriptionId** | [**number**] |  | defaults to undefined|
| **extensionCount** | [**number**] |  | defaults to undefined|


### Return type

**string**

### Authorization

[oauth2](../README.md#oauth2)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getAlreadyPaidGroup**
> ResponseTotalAnalyticSubscriptionGroupDto getAlreadyPaidGroup()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let groupId: number; // (default to undefined)

const { status, data } = await apiInstance.getAlreadyPaidGroup(
    groupId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **groupId** | [**number**] |  | defaults to undefined|


### Return type

**ResponseTotalAnalyticSubscriptionGroupDto**

### Authorization

[oauth2](../README.md#oauth2)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getAnalyticSubscriptionById**
> ResponseTotalAnalyticSubscriptionDto getAnalyticSubscriptionById()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let groupId: number; // (default to undefined)
let subscriptionId: number; // (default to undefined)

const { status, data } = await apiInstance.getAnalyticSubscriptionById(
    groupId,
    subscriptionId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **groupId** | [**number**] |  | defaults to undefined|
| **subscriptionId** | [**number**] |  | defaults to undefined|


### Return type

**ResponseTotalAnalyticSubscriptionDto**

### Authorization

[oauth2](../README.md#oauth2)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getPaymentInfoSubscriptionById**
> ResponseAnalyticPaymentSubscriptionsDto getPaymentInfoSubscriptionById()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let groupId: number; // (default to undefined)
let subscriptionId: number; // (default to undefined)
let page: number; // (default to undefined)

const { status, data } = await apiInstance.getPaymentInfoSubscriptionById(
    groupId,
    subscriptionId,
    page
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **groupId** | [**number**] |  | defaults to undefined|
| **subscriptionId** | [**number**] |  | defaults to undefined|
| **page** | [**number**] |  | defaults to undefined|


### Return type

**ResponseAnalyticPaymentSubscriptionsDto**

### Authorization

[oauth2](../README.md#oauth2)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getSubscriptionById**
> ResponseSubscriptionsDto getSubscriptionById()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let groupId: number; // (default to undefined)
let subscriptionId: number; // (default to undefined)

const { status, data } = await apiInstance.getSubscriptionById(
    groupId,
    subscriptionId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **groupId** | [**number**] |  | defaults to undefined|
| **subscriptionId** | [**number**] |  | defaults to undefined|


### Return type

**ResponseSubscriptionsDto**

### Authorization

[oauth2](../README.md#oauth2)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getSubscriptionsGroup**
> ResponseSubscriptionsDto getSubscriptionsGroup()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let groupId: number; // (default to undefined)
let page: number; // (default to undefined)

const { status, data } = await apiInstance.getSubscriptionsGroup(
    groupId,
    page
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **groupId** | [**number**] |  | defaults to undefined|
| **page** | [**number**] |  | defaults to undefined|


### Return type

**ResponseSubscriptionsDto**

### Authorization

[oauth2](../README.md#oauth2)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getTotalAnalyticGroups**
> ResponseTotalAnalyticGroupsDto getTotalAnalyticGroups()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

const { status, data } = await apiInstance.getTotalAnalyticGroups();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**ResponseTotalAnalyticGroupsDto**

### Authorization

[oauth2](../README.md#oauth2)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **saveSubscription**
> ResponseSubscriptionDto saveSubscription(requestSubscriptionDto)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    RequestSubscriptionDto
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let groupId: number; // (default to undefined)
let requestSubscriptionDto: RequestSubscriptionDto; //

const { status, data } = await apiInstance.saveSubscription(
    groupId,
    requestSubscriptionDto
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **requestSubscriptionDto** | **RequestSubscriptionDto**|  | |
| **groupId** | [**number**] |  | defaults to undefined|


### Return type

**ResponseSubscriptionDto**

### Authorization

[oauth2](../README.md#oauth2)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**201** | Created |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **updateSubscription**
> string updateSubscription(requestUpdateSubscriptionDto)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    RequestUpdateSubscriptionDto
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let groupId: number; // (default to undefined)
let subscriptionId: number; // (default to undefined)
let requestUpdateSubscriptionDto: RequestUpdateSubscriptionDto; //

const { status, data } = await apiInstance.updateSubscription(
    groupId,
    subscriptionId,
    requestUpdateSubscriptionDto
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **requestUpdateSubscriptionDto** | **RequestUpdateSubscriptionDto**|  | |
| **groupId** | [**number**] |  | defaults to undefined|
| **subscriptionId** | [**number**] |  | defaults to undefined|


### Return type

**string**

### Authorization

[oauth2](../README.md#oauth2)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

