/*
 * Copyright 2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may
 * not use this file except in compliance with the License. A copy of the
 * License is located at
 *
 *     http://aws.amazon.com/apache2.0/
 *
 * or in the "LICENSE" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.blox.schedulingmanager.deployment.steps;

import com.amazonaws.AmazonClientException;
import com.amazonaws.blox.schedulingmanager.deployment.handler.Encoder;
import com.amazonaws.blox.schedulingmanager.deployment.steps.types.StateData;
import com.amazonaws.blox.schedulingmanager.deployment.steps.types.TaskWorkflowInput;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.model.StartExecutionRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class StartDeployment implements StepHandler {

  private static final String START_TASK_WF_ARN_ENV_VAR = "START_TASK_WF_ARN";

  private Encoder encoder;
  private AWSStepFunctions stepFunctions;

  @Override
  public void handleRequest(InputStream input, OutputStream output, Context context)
      throws IOException {
    log.debug("startDeployment lambda");

    final StateData stateData = encoder.decode(input, StateData.class);

    final TaskWorkflowInput taskWorkflowInput =
        TaskWorkflowInput.builder().taskDefinition("taskDef").build();

    final String taskWorkflowInputJson = encoder.encode(taskWorkflowInput);

    //TODO: spawn workflows for each task
    //TODO: don't hardcode the statemachine arn or replace accountid/region
    final StartExecutionRequest startExecutionRequest =
        new StartExecutionRequest()
            .withStateMachineArn(System.getenv(START_TASK_WF_ARN_ENV_VAR))
            .withInput(taskWorkflowInputJson)
            .withName("StartTaskWorkflow" + UUID.randomUUID().toString());
    try {
      stepFunctions.startExecution(startExecutionRequest);
    } catch (final AmazonClientException e) {
      log.error("StartTask workflow failed to start", e);
      throw e;
    }
  }
}
