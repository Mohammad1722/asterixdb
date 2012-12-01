/*
 * Copyright 2009-2012 by The Regents of the University of California
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License from
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.uci.ics.asterix.algebra.operators.physical;

import java.util.List;

import org.apache.commons.lang3.mutable.Mutable;

import edu.uci.ics.asterix.transaction.management.service.transaction.JobId;
import edu.uci.ics.hyracks.algebricks.common.exceptions.AlgebricksException;
import edu.uci.ics.hyracks.algebricks.core.algebra.base.ILogicalExpression;
import edu.uci.ics.hyracks.algebricks.runtime.base.IPushRuntime;
import edu.uci.ics.hyracks.algebricks.runtime.base.IPushRuntimeFactory;
import edu.uci.ics.hyracks.api.context.IHyracksTaskContext;
import edu.uci.ics.hyracks.api.dataflow.value.IBinaryHashFunctionFactory;

public class CommitRuntimeFactory implements IPushRuntimeFactory {

    private static final long serialVersionUID = 1L;

    private final JobId jobId;
    private final int datasetId;
    private final int[] primaryKeyFields;
    IBinaryHashFunctionFactory[] binaryHashFunctionFactories;
    private final boolean isWriteTransaction;

    public CommitRuntimeFactory(JobId jobId, int datasetId, int[] primaryKeyFields,
            IBinaryHashFunctionFactory[] binaryHashFunctionFactories, boolean isWriteTransaction) {
        this.jobId = jobId;
        this.datasetId = datasetId;
        this.primaryKeyFields = primaryKeyFields;
        this.binaryHashFunctionFactories = binaryHashFunctionFactories;
        this.isWriteTransaction = isWriteTransaction;
    }

    @Override
    public String toString() {
        return "commit";
    }

    @Override
    public IPushRuntime createPushRuntime(IHyracksTaskContext ctx) throws AlgebricksException {
        return new CommitRuntime(ctx, jobId, datasetId, primaryKeyFields, binaryHashFunctionFactories, isWriteTransaction);
    }
}
