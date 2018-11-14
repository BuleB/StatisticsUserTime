# StatisticsUserTime

用户时常统计：

[TOC]

### 需求

统计用户使用的时长，上报结算时的时间戳，使用时常，用户id或者设备id，上报时机用户切入后台时

### 流程图


```
graph TD
A[用户进入app]-->|计时|B{用户使用}
B-->C[App进入后台]
B-->D[App崩溃]
D-->|结算|F[存储时间戳和使用时长,累计下次结算]
C-->|结算|E[上报数据]
F-->C

```

### 实现

#### 涉及知识

- App进入后台回调
- 崩溃回调

主要逻辑:

每次进入前台的时候记录foregroundTs,每次进入onResume,onPause时记录用户活跃时间戳activeTs,当进入后台的时候结算,activeTs-foregroundTs=reduceTime(用户使用时间),进入前台和进入后台只会触发一次,activeTs会根据生命周期会不停的向前跑,避免了写个定时器一直计算.

问题: 为什么onResume和OnPause会触发activeTs,

activeTs是个保证用户还在活跃使用的时间戳,onResume调用,防止奔溃时,activeTs误差,因为崩溃并不走正常的生命周期,onPause调用是为了在走退入后台逻辑使用.




##### App进入后台回调

使用ActivityLifecycleCallbacks,进行生命周期的监听.优点就是进入性小,如果之前有做过手势解锁的需求的话,那么这个功能和其一致.

首先看生命周期,很容易知道onResume应用肯定在前台,那么如何定义应用程序在后台,这里出现了一个问题,在后台的时候肯定走了onStop,但是打开一个新的Activity也会走OnStop.带着这个问题我们继续往下缕.

我们先从一个Activity类推:
A启动走了下面的生命周期,我们完全可以通过在onResume时记录在前台,在onStop在后台.

```
graph TD
A[onCreate]-->B[onStart]
B-->C[onResume]
C-->D[onPause]
D-->E[onStop]
E-->G[onRestart]
G-->B
D-->C
E-->F[onDestory]
```

当两个Activity互相跳转:
A->B
```
graph TD

A[A:onPause]-->B[B:onCreate]

B-->C[B:onStart]
C-->D[B:onResume]
D-->E[A:onStop]

```
当进入后台的时候我们可以观察到onStart 和 onStop 一直是成对存在的,一个Activity的时候从创建到销毁出现了一对,
当两个Activity的时候,A->B,onStart 出现了两次 onStop出现了一次,当B按了home键,也是出现了两对,而且无论如何,当成对出现的时候都是进入后台的时候!这个可以随便推敲.
所以我们只需要在onStart +1 在onStop -1 然后判断是否=0就可以了解在没在后台.

##### 崩溃回调

因为崩溃会不走生命周期,所以我们这个要单独走个逻辑,这里涉及到:Thread.UncaughtExceptionHandler,次线程出现问题的时候 throw的异常会走这个回调,在这里进行统计结算

```java
  public void register() {

        if (Thread.getDefaultUncaughtExceptionHandler() == this) {
            return;
        }

        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.d(TAG, "uncaughtException: ");
        StatisticalTimeUtils.getInstance().saveActiveTs();
        StatisticalTimeUtils.getInstance().clearAll();
         if (mDefaultHandler != null && mDefaultHandler != Thread.getDefaultUncaughtExceptionHandler()) {
            mDefaultHandler.uncaughtException(t, e);
        }
    }
```

##### 代码

[github地址](https://github.com/BuleB/StatisticsUserTime)


#### 最后

主要的思路和代码的主要部分都是来自于[如何精确计算Android应用的使用时长](https://www.jianshu.com/p/9f285441a384),由于作者没有给到所有代码,我这里是自己实现了一些没有的代码
