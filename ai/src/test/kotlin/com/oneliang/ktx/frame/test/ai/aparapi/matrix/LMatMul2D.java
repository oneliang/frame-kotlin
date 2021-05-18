/**
 * Copyright (c) 2016 - 2018 Syncleus, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * This product currently only contains code developed by authors
 * of specific components, as identified by the source code files.
 * <p>
 * Since product implements StAX API, it has dependencies to StAX API
 * classes.
 * <p>
 * For additional credits (generally to people who reported problems)
 * see CREDITS file.
 */
/**
 * This product currently only contains code developed by authors
 * of specific components, as identified by the source code files.
 *
 * Since product implements StAX API, it has dependencies to StAX API
 * classes.
 *
 * For additional credits (generally to people who reported problems)
 * see CREDITS file.
 */
package com.oneliang.ktx.frame.test.ai.aparapi.matrix;

import com.aparapi.Kernel;

class LMatMul2D extends Kernel {
    long[][] A;

    long[][] B;

    long[][] C;

    int N;

    public LMatMul2D(long[][] A, long[][] B, long[][] C, int N) {
        this.A = A;
        this.B = B;
        this.C = C;
        this.N = N;
    }

    @Override
    public void run() {
        int id = getGlobalId();
        int i = id / N;
        int j = id % N;
        for (int k = 0; k < N; k++) {
//            double a = java.lang.Double.longBitsToDouble(A[i][k]);
//            double b = java.lang.Double.longBitsToDouble(B[k][j]);
//            double c = a * b;
//            C[i][j] += java.lang.Double.doubleToLongBits(c);//A[i][k] * B[k][j];
            C[i][j] += A[i][k] * B[k][j];
        }
    }
}
