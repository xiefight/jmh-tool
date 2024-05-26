/*
 * Copyright (c) 2005, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package cn.xt.jmh;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

//将方法上打上@Benchmark注解，表示该方法是需要测试的
//预热次数，时间
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
//当前启动多少个进程，可以追加虚拟机参数
@Fork(value = 1, jvmArgs = {"-Xms1g", "-Xmx1g"})
//指定显示的结果，吞吐量Throughput、执行耗时AverageTime
@BenchmarkMode(Mode.AverageTime)
//指定显示结果单位 NANOSECONDS、MICROSECONDS、MILLISECONDS、SECONDS
@OutputTimeUnit(TimeUnit.NANOSECONDS)
//变量共享范围    Scope.Thread
@State(Scope.Benchmark)
public class MyJITBenchmark {

    public int add(int a, int b) {
        return a + b;
    }

    public int jitTest(){
        int sum = 0;
        for(int i = 0;i < 1000000;i++){
            sum = add(sum, 100);
        }
        return sum;
    }

    //禁用JIT
    @Benchmark
    @Fork(value = 1, jvmArgsPrepend = {"-Xint"})
    public void testNoJIT(Blackhole blackhole){
        int i = jitTest();
        //防止死代码，要么返回i，要么使用blackhole消费i
        blackhole.consume(i);
    }

    //启用JIT
    @Benchmark
//    @Fork(value = 1)
    public void testJIT(Blackhole blackhole){
        int i = jitTest();
        //防止死代码，要么返回i，要么使用blackhole消费i
        blackhole.consume(i);
    }

    //只使用C1编译器 1层
    @Benchmark
//    @Fork(value = 1, jvmArgsPrepend = {"-XX:+UnlockExperimentalVMOptions", "-XX:+UseC1Compiler"})
    @Fork(value = 1, jvmArgsPrepend = {"-XX:TieredStopAtLevel=1"})
    public void testC1(Blackhole blackhole){
        int i = jitTest();
        //防止死代码，要么返回i，要么使用blackhole消费i
        blackhole.consume(i);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MyJITBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }


}
