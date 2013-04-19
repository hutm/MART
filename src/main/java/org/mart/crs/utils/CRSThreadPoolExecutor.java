/*
 * Copyright (c) 2008-2013 Maksim Khadkevich and Fondazione Bruno Kessler.
 *
 * This file is part of MART.
 * MART is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2, as published
 * by the Free Software Foundation.
 *
 * MART is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with MART; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.mart.crs.utils;

import org.apache.log4j.Logger;
import org.mart.crs.logging.CRSLogger;
import org.mart.crs.utils.helper.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @version 1.0 Feb 4, 2010 11:14:44 AM
 * @author: Maksim Khadkevich
 */


public class CRSThreadPoolExecutor {

    protected static Logger logger = CRSLogger.getLogger(CRSThreadPoolExecutor.class);

    protected ExecutorService pool;
    protected java.util.List<Future<Runnable>> tasks;

    public CRSThreadPoolExecutor(int poolSize) {
        logger.info("Number of available processors = " + poolSize);
        pool = Executors.newFixedThreadPool(poolSize);
        tasks = new ArrayList<Future<Runnable>>();
    }

    public void runTask(Runnable task) {
        Future<Runnable> future = pool.submit(task, task);
        tasks.add(future);
    }

    public void waitCompletedAndshutDown() {
        try {
            for (Future<Runnable> future : tasks) {
                Runnable e = future.get();
            }
            pool.shutdown();
        } catch (ExecutionException e) {
            logger.fatal("FATAL ERROR OCCURED:");
            logger.fatal(Helper.getStackTrace(e));
            throw new RuntimeException(e);
        } catch (InterruptedException ie) {
            logger.fatal("FATAL ERROR OCCURED:");
            logger.fatal(Helper.getStackTrace(ie));
            throw new RuntimeException(ie);
        }
    }

    public List<Future<Runnable>> getTasks() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for(Future<Runnable> future:tasks){
                if(future.isDone()){
                    tasks.remove(future);
                    changed = true;
                    break;
                }
            }
        }
        return tasks;
    }
}
