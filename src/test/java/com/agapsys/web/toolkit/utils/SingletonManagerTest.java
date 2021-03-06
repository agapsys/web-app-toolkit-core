/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agapsys.web.toolkit.utils;

import org.junit.Assert;
import org.junit.Test;

public class SingletonManagerTest {

    public static interface RootInterface {}

    public static abstract class AbstractBase implements RootInterface {}

    public static class BaseClass extends AbstractBase {
        @Override
        public String toString() {
            return this.getClass().getSimpleName();
        }
    }

    public static class SubClass extends BaseClass {}

    public static class SubSubClass extends SubClass {}

    private static class DeepClass extends SubSubClass {}

    @Test
    public void registerInstanceHierarchyTest() {
        SingletonManager sm = new SingletonManager<>(RootInterface.class);

        SubSubClass ssc = new SubSubClass();
        sm.registerInstance(ssc);

        Assert.assertEquals(3, sm.getClasses().size()); // <-- BaseClass, SubClass, and SubSubClass (the others are ignored since they are not concrete)
        Assert.assertEquals(1, sm.getInstances().size());

        Assert.assertTrue(sm.getClasses().contains(BaseClass.class));
        Assert.assertTrue(sm.getClasses().contains(SubClass.class));
        Assert.assertTrue(sm.getClasses().contains(SubSubClass.class));

        Assert.assertNull(sm.getInstance(RootInterface.class));
        Assert.assertNull(sm.getInstance(AbstractBase.class));

        Assert.assertSame(ssc, sm.getInstance(BaseClass.class));
        Assert.assertSame(ssc, sm.getInstance(SubClass.class));
        Assert.assertSame(ssc, sm.getInstance(SubSubClass.class));
        // ---------------------------------------------------------------------
        sm = new SingletonManager<>(SubClass.class);
        sm.registerInstance(ssc);

        Assert.assertEquals(2, sm.getClasses().size()); // <-- SubSubClass and SubClass (BaseClass is above SubClass. The others are ignored since they are not concrete)
        Assert.assertEquals(1, sm.getInstances().size());

        Assert.assertTrue(sm.getClasses().contains(SubSubClass.class));
        Assert.assertTrue(sm.getClasses().contains(SubClass.class));

        Assert.assertNull(sm.getInstance(RootInterface.class));
        Assert.assertNull(sm.getInstance(AbstractBase.class));
        Assert.assertNull(sm.getInstance(BaseClass.class));

        Assert.assertSame(ssc, sm.getInstance(SubClass.class));
        Assert.assertSame(ssc, sm.getInstance(SubSubClass.class));
        // ---------------------------------------------------------------------
        sm = new SingletonManager<>(SubSubClass.class);
        sm.registerInstance(ssc);

        Assert.assertEquals(1, sm.getClasses().size()); // <-- SubSubClass (BaseClass and SubClass are above SubSubClass. The others are ignored since they are not concrete)
        Assert.assertEquals(1, sm.getInstances().size());

        Assert.assertTrue(sm.getClasses().contains(SubSubClass.class));

        Assert.assertNull(sm.getInstance(RootInterface.class));
        Assert.assertNull(sm.getInstance(AbstractBase.class));
        Assert.assertNull(sm.getInstance(BaseClass.class));
        Assert.assertNull(sm.getInstance(SubClass.class));

        Assert.assertSame(ssc, sm.getInstance(SubSubClass.class));
    }

    @Test
    public void ensureSingletonTestPassingInstance() {
        SingletonManager sm = new SingletonManager<>(BaseClass.class);

        SubSubClass ssc = new SubSubClass();
        sm.registerInstance(ssc);

        Assert.assertSame(ssc, sm.getInstance(BaseClass.class));
        Assert.assertSame(ssc, sm.getInstance(BaseClass.class));   // <-- consecutive calls should return the same instance

        Assert.assertSame(ssc, sm.getInstance(SubClass.class));
        Assert.assertSame(ssc, sm.getInstance(SubClass.class));    // <-- consecutive calls should return the same instance

        Assert.assertSame(ssc, sm.getInstance(SubSubClass.class));
        Assert.assertSame(ssc, sm.getInstance(SubSubClass.class)); // <-- consecutive calls should return the same instance
    }

    @Test
    public void ensureSingletonTestPassingClass() {
        SingletonManager sm = new SingletonManager<>(BaseClass.class);

        SubSubClass ssc = new SubSubClass();
        sm.registerClass(SubSubClass.class);

        Assert.assertNotSame(ssc, sm.getInstance(SubSubClass.class)); // <-- ssc was created explicitly

        BaseClass bc = (BaseClass) sm.getInstance(BaseClass.class);
        Assert.assertSame(bc, sm.getInstance(BaseClass.class));
        Assert.assertSame(bc, sm.getInstance(BaseClass.class));   // <-- consecutive calls should return the same instance

        Assert.assertSame(bc, sm.getInstance(SubClass.class));
        Assert.assertSame(bc, sm.getInstance(SubClass.class));    // <-- consecutive calls should return the same instance

        Assert.assertSame(bc, sm.getInstance(SubSubClass.class));
        Assert.assertSame(bc, sm.getInstance(SubSubClass.class)); // <-- consecutive calls should return the same instance
    }

    @Test(expected = RuntimeException.class)
    public void checkConstructorAccess() {
        SingletonManager sm = new SingletonManager<>(DeepClass.class);
        sm.registerClass(DeepClass.class); // <-- DeepClass constructor is private
    }

    @Test
    public void replaceInstance() {
        SingletonManager sm = new SingletonManager<>(BaseClass.class);

        SubSubClass ssc = new SubSubClass();
        sm.registerInstance(ssc);
        
        Assert.assertSame(ssc, sm.getInstance(BaseClass.class));
        Assert.assertSame(ssc, sm.getInstance(SubClass.class));
        Assert.assertSame(ssc, sm.getInstance(SubSubClass.class));
        
        SubClass sc = new SubClass();
        sm.registerInstance(sc); // <-- hierarchy will be overridden

        Assert.assertSame(sc, sm.getInstance(BaseClass.class));
        Assert.assertSame(sc, sm.getInstance(SubClass.class));
        Assert.assertNull(sm.getInstance(SubSubClass.class));
    }
    
    @Test
    public void skipHierarcyOverride() {
        SingletonManager<BaseClass> sm = new SingletonManager<>(BaseClass.class);
        SubClass scInstance = new SubClass();
        
        BaseClass bcInstance = sm.getInstance(BaseClass.class);

        // Skiping class hierarchy override...
        sm.registerInstance(scInstance, false);
        
        Assert.assertSame(bcInstance, sm.getInstance(BaseClass.class));
        Assert.assertNotSame(bcInstance, sm.getInstance(SubClass.class));

        Assert.assertNotSame(scInstance, sm.getInstance(BaseClass.class));
        Assert.assertSame(scInstance, sm.getInstance(SubClass.class));
        
        // Overriding class hierarchy...
        sm.registerInstance(scInstance, true);
        Assert.assertSame(scInstance, sm.getInstance(BaseClass.class));
        Assert.assertSame(scInstance, sm.getInstance(SubClass.class));
    }
}
