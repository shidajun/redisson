/**
 * Copyright 2018 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.transaction.operation.set;

import org.redisson.RedissonSet;
import org.redisson.api.RObject;
import org.redisson.api.RSet;
import org.redisson.client.codec.Codec;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.transaction.operation.TransactionalOperation;

/**
 * 
 * @author Nikita Koksharov
 *
 */
public class MoveOperation extends TransactionalOperation {

    private String destinationName;
    private Object value;
    private long threadId;
    
    public MoveOperation(RObject set, String destinationName, long threadId, Object value) {
        this(set.getName(), set.getCodec(), destinationName, threadId, value);
    }
    
    public MoveOperation(String name, Codec codec, String destinationName, long threadId, Object value) {
        super(name, codec);
        this.destinationName = destinationName;
        this.value = value;
        this.threadId = threadId;
    }

    @Override
    public void commit(CommandAsyncExecutor commandExecutor) {
        RSet<Object> set = new RedissonSet<Object>(codec, commandExecutor, name, null);
        RSet<Object> destinationSet = new RedissonSet<Object>(codec, commandExecutor, destinationName, null);
        set.moveAsync(destinationSet.getName(), value);
        destinationSet.getLock(value).unlockAsync(threadId);
        set.getLock(value).unlockAsync(threadId);
    }

    @Override
    public void rollback(CommandAsyncExecutor commandExecutor) {
        RSet<Object> set = new RedissonSet<Object>(codec, commandExecutor, name, null);
        RSet<Object> destinationSet = new RedissonSet<Object>(codec, commandExecutor, destinationName, null);
        destinationSet.getLock(value).unlockAsync(threadId);
        set.getLock(value).unlockAsync(threadId);
    }
    
    public String getDestinationName() {
        return destinationName;
    }
    
    public Object getValue() {
        return value;
    }
    
    public long getThreadId() {
        return threadId;
    }

}
