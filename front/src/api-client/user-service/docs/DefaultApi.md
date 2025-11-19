# DefaultApi

All URIs are relative to *http://localhost:8000*

|Method | HTTP request | Description|
|------------- | ------------- | -------------|
|[**changesOwnerGroups**](#changesownergroups) | **PATCH** /api/v1/groups/change-owners | Изменения владельца группы/групп|
|[**checkUserInGroup**](#checkuseringroup) | **GET** /api/v1/groups/{groupId}/members/check | Проверка пользователь член группы|
|[**checkUserIsOwnerGroup**](#checkuserisownergroup) | **GET** /api/v1/groups/{groupId}/members/check/owner | Проверка пользователь собственник группы|
|[**checkVerifyEmail**](#checkverifyemail) | **GET** /api/v1/users/me/verify_email | Проверка верификации почты пользователя|
|[**createGroup**](#creategroup) | **POST** /api/v1/groups | Создание группы|
|[**deleteGroup**](#deletegroup) | **DELETE** /api/v1/groups/{groupId} | Удаления группы|
|[**deleteMember**](#deletemember) | **DELETE** /api/v1/groups/{groupId}/members/{userId} | Удаление пользователя из группы|
|[**deleteUser**](#deleteuser) | **DELETE** /api/v1/users/me | Удаления пользователя|
|[**getCodeByGroupId**](#getcodebygroupid) | **GET** /api/v1/invitations/{groupId} | Получения кода приглашения|
|[**getGroupById**](#getgroupbyid) | **GET** /api/v1/groups/{groupId} | Получение группы|
|[**getGroups**](#getgroups) | **GET** /api/v1/groups/owner/me | Получение групп созданных пользователем|
|[**getGroupsUserIsMember**](#getgroupsuserismember) | **GET** /api/v1/groups/me | Получение групп: пользователь член группы|
|[**getMembers**](#getmembers) | **GET** /api/v1/groups/{groupId}/members | Получение членов группы|
|[**getOwnerIdByGroupId**](#getowneridbygroupid) | **GET** /api/v1/groups/{groupId}/owner | Получение id собственника группы|
|[**getUserById**](#getuserbyid) | **GET** /api/v1/users/me | Получение пользователя|
|[**joinGroupByCode**](#joingroupbycode) | **POST** /api/v1/invitations/join | Присоединение к группе по коду приглашения|
|[**updateGroup**](#updategroup) | **PATCH** /api/v1/groups/{groupId} | Обновление информации группы|
|[**updateUser**](#updateuser) | **PATCH** /api/v1/users/me | Обновление информации пользователя|

# **changesOwnerGroups**
> { [key: string]: string; } changesOwnerGroups(requestGroupsOwnershipChangesDto)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    RequestGroupsOwnershipChangesDto
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let requestGroupsOwnershipChangesDto: RequestGroupsOwnershipChangesDto; //

const { status, data } = await apiInstance.changesOwnerGroups(
    requestGroupsOwnershipChangesDto
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **requestGroupsOwnershipChangesDto** | **RequestGroupsOwnershipChangesDto**|  | |


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

# **checkUserInGroup**
> { [key: string]: boolean; } checkUserInGroup()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let groupId: number; // (default to undefined)

const { status, data } = await apiInstance.checkUserInGroup(
    groupId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **groupId** | [**number**] |  | defaults to undefined|


### Return type

**{ [key: string]: boolean; }**

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

# **checkUserIsOwnerGroup**
> { [key: string]: boolean; } checkUserIsOwnerGroup()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let groupId: number; // (default to undefined)

const { status, data } = await apiInstance.checkUserIsOwnerGroup(
    groupId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **groupId** | [**number**] |  | defaults to undefined|


### Return type

**{ [key: string]: boolean; }**

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

# **checkVerifyEmail**
> Array<ResponseVerifyEmailDto> checkVerifyEmail()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

const { status, data } = await apiInstance.checkVerifyEmail();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<ResponseVerifyEmailDto>**

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

# **createGroup**
> ResponseGroupDto createGroup(requestGroupDto)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    RequestGroupDto
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let requestGroupDto: RequestGroupDto; //

const { status, data } = await apiInstance.createGroup(
    requestGroupDto
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **requestGroupDto** | **RequestGroupDto**|  | |


### Return type

**ResponseGroupDto**

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

# **deleteGroup**
> deleteGroup()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let groupId: number; // (default to undefined)

const { status, data } = await apiInstance.deleteGroup(
    groupId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **groupId** | [**number**] |  | defaults to undefined|


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
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **deleteMember**
> deleteMember()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let groupId: number; // (default to undefined)
let userId: string; // (default to undefined)

const { status, data } = await apiInstance.deleteMember(
    groupId,
    userId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **groupId** | [**number**] |  | defaults to undefined|
| **userId** | [**string**] |  | defaults to undefined|


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
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **deleteUser**
> deleteUser()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

const { status, data } = await apiInstance.deleteUser();
```

### Parameters
This endpoint does not have any parameters.


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
|**200** | OK |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **getCodeByGroupId**
> ResponseInviteCodeDto getCodeByGroupId()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let groupId: number; // (default to undefined)

const { status, data } = await apiInstance.getCodeByGroupId(
    groupId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **groupId** | [**number**] |  | defaults to undefined|


### Return type

**ResponseInviteCodeDto**

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

# **getGroupById**
> Array<ResponseGroupDto> getGroupById()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let groupId: number; // (default to undefined)

const { status, data } = await apiInstance.getGroupById(
    groupId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **groupId** | [**number**] |  | defaults to undefined|


### Return type

**Array<ResponseGroupDto>**

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

# **getGroups**
> Array<ResponseGroupsDto> getGroups()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

const { status, data } = await apiInstance.getGroups();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<ResponseGroupsDto>**

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

# **getGroupsUserIsMember**
> Array<ResponseGroupsDto> getGroupsUserIsMember()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let page: number; // (default to undefined)

const { status, data } = await apiInstance.getGroupsUserIsMember(
    page
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **page** | [**number**] |  | defaults to undefined|


### Return type

**Array<ResponseGroupsDto>**

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

# **getMembers**
> ResponseMembersDto getMembers()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let groupId: number; // (default to undefined)

const { status, data } = await apiInstance.getMembers(
    groupId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **groupId** | [**number**] |  | defaults to undefined|


### Return type

**ResponseMembersDto**

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

# **getOwnerIdByGroupId**
> Array<ResponseGroupOwnerIdDto> getOwnerIdByGroupId()

Получение id пользователя доступно только при межсерверном взаимодействии

### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let groupId: number; // (default to undefined)

const { status, data } = await apiInstance.getOwnerIdByGroupId(
    groupId
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **groupId** | [**number**] |  | defaults to undefined|


### Return type

**Array<ResponseGroupOwnerIdDto>**

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

# **getUserById**
> Array<ResponseUserDto> getUserById()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

const { status, data } = await apiInstance.getUserById();
```

### Parameters
This endpoint does not have any parameters.


### Return type

**Array<ResponseUserDto>**

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

# **joinGroupByCode**
> ResponseMembersDto joinGroupByCode()


### Example

```typescript
import {
    DefaultApi,
    Configuration
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let code: string; // (default to undefined)

const { status, data } = await apiInstance.joinGroupByCode(
    code
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **code** | [**string**] |  | defaults to undefined|


### Return type

**ResponseMembersDto**

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

# **updateGroup**
> { [key: string]: string; } updateGroup(requestUpdateGroupDto)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    RequestUpdateGroupDto
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let groupId: number; // (default to undefined)
let requestUpdateGroupDto: RequestUpdateGroupDto; //

const { status, data } = await apiInstance.updateGroup(
    groupId,
    requestUpdateGroupDto
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **requestUpdateGroupDto** | **RequestUpdateGroupDto**|  | |
| **groupId** | [**number**] |  | defaults to undefined|


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

# **updateUser**
> { [key: string]: string; } updateUser(requestUpdateUserDto)


### Example

```typescript
import {
    DefaultApi,
    Configuration,
    RequestUpdateUserDto
} from './api';

const configuration = new Configuration();
const apiInstance = new DefaultApi(configuration);

let requestUpdateUserDto: RequestUpdateUserDto; //

const { status, data } = await apiInstance.updateUser(
    requestUpdateUserDto
);
```

### Parameters

|Name | Type | Description  | Notes|
|------------- | ------------- | ------------- | -------------|
| **requestUpdateUserDto** | **RequestUpdateUserDto**|  | |


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

