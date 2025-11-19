# DefaultApi

All URIs are relative to *http://localhost:8002*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**getCountNotificationsNew**](#getcountnotificationsnew) | **GET** /api/v1/notifications/count | Получение количество не прочитанных уведомлений пользователя|
|[**getNewNotifications**](#getnewnotifications) | **GET** /api/v1/notifications/new | Получение непрочитанных уведомлений пользователя|
|[**getOldNotifications**](#getoldnotifications) | **GET** /api/v1/notifications/old | Получение прочитанных уведомлений пользователя|
|[**updateNotifications**](#updatenotifications) | **PATCH** /api/v1/notifications | Обновление уведомлений (прочитать)|

# **getCountNotificationsNew**
> ResponseNotificationsCountDto getCountNotificationsNew()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

const { status, data } = await apiInstance.getCountNotificationsNew();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**ResponseNotificationsCountDto**

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

# **getNewNotifications**
> ResponseNotificationsDto getNewNotifications()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let page: number; // (default to undefined)

const { status, data } = await apiInstance.getNewNotifications(
    page
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **page** | [**number**] |  | defaults to undefined|


### Return type

**ResponseNotificationsDto**

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

# **getOldNotifications**
> ResponseNotificationsDto getOldNotifications()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let page: number; // (default to undefined)

const { status, data } = await apiInstance.getOldNotifications(
    page
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **page** | [**number**] |  | defaults to undefined|


### Return type

**ResponseNotificationsDto**

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

# **updateNotifications**
> { [key: string]: string; } updateNotifications(requestUpdateNotificationsDto)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    RequestUpdateNotificationsDto
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let requestUpdateNotificationsDto: RequestUpdateNotificationsDto; //

const { status, data } = await apiInstance.updateNotifications(
    requestUpdateNotificationsDto
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **requestUpdateNotificationsDto** | **RequestUpdateNotificationsDto**|  | |


### Return type

**{ [key: string]: string; }**

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

