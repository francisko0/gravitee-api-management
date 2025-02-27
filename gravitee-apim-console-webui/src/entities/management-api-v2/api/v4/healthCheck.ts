/*
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { Pagination } from '../../pagination';
import { Links } from '../../links';

export interface ApiHealthResponseTimeOvertime {
  timeRange: {
    from: number;
    to: number;
    interval: number;
  };
  data: number[];
}

export interface ApiAvailability {
  global: number;
  group: {
    [key: string]: number;
  }[];
}

export interface ApiAverageResponseTime {
  global: number;
  group: {
    [key: string]: number;
  }[];
}

export interface HealthCheckLogsRequestParams {
  from: number;
  to: number;
  page: number;
  perPage: number;
  success: boolean;
}

export interface HealthCheckLogsResponse {
  data?: HealthCheckLog[];
  pagination?: Pagination;
  links?: Links;
}

export interface HealthCheckLog {
  id: string;
  timestamp: string;
  endpointName: string;
  gatewayId: string;
  responseTime: number;
  success: boolean;
  steps: HealthCheckStep[];
}

export interface HealthCheckStep {
  name: string;
  success: string;
  message: string;
  request: HealthCheckStepRequest;
  response: HealthCheckStepResponse;
}

export interface HealthCheckStepRequest {
  uri: string;
  method: string;
  headers: {
    [key: string]: string;
  };
}

export interface HealthCheckStepResponse {
  status: number;
  body: string;
  headers: {
    [key: string]: string;
  };
}

export enum FieldParameter {
  endpoint = 'endpoint',
  gateway = 'gateway',
}
