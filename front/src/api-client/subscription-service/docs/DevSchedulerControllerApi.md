# DevSchedulerControllerApi

All URIs are relative to *http://localhost:8001*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**generateSubscriptionsAnalytic**](#generatesubscriptionsanalytic) | **POST** /scheduler/dev/generate/analytic | |
|[**sendMessageWithAlreadyPaymentInfo**](#sendmessagewithalreadypaymentinfo) | **POST** /scheduler/dev/send/message/payment/already | |
|[**sendMessageWithPaymentInfo**](#sendmessagewithpaymentinfo) | **POST** /scheduler/dev/send/message/payment | |
|[**updateStatusSubscriptions**](#updatestatussubscriptions) | **POST** /scheduler/dev/update/status/subscription | |

# **generateSubscriptionsAnalytic**
> { [key: string]: string; } generateSubscriptionsAnalytic()


### Example

```typescript
import {
    DevSchedulerControllerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DevSchedulerControllerApi(configuration);

const { status, data } = await apiInstance.generateSubscriptionsAnalytic();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**{ [key: string]: string; }**

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

# **sendMessageWithAlreadyPaymentInfo**
> { [key: string]: string; } sendMessageWithAlreadyPaymentInfo()


### Example

```typescript
import {
    DevSchedulerControllerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DevSchedulerControllerApi(configuration);

const { status, data } = await apiInstance.sendMessageWithAlreadyPaymentInfo();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**{ [key: string]: string; }**

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

# **sendMessageWithPaymentInfo**
> { [key: string]: string; } sendMessageWithPaymentInfo()


### Example

```typescript
import {
    DevSchedulerControllerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DevSchedulerControllerApi(configuration);

const { status, data } = await apiInstance.sendMessageWithPaymentInfo();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**{ [key: string]: string; }**

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

# **updateStatusSubscriptions**
> { [key: string]: string; } updateStatusSubscriptions()


### Example

```typescript
import {
    DevSchedulerControllerApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DevSchedulerControllerApi(configuration);

const { status, data } = await apiInstance.updateStatusSubscriptions();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**{ [key: string]: string; }**

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

