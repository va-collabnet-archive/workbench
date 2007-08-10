package org.dwfa.bpa.tasks.transaction;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.util.Collection;

import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.transaction.CannotAbortException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.UnknownTransactionException;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = 
{ @Spec(directory = "tasks/transaction", type = BeanType.TASK_BEAN)})
public class AbortWorkflowTransaction extends AbstractTask {

   private static final long serialVersionUID = 1;

   private static final int dataVersion = 1;

   private void writeObject(ObjectOutputStream out) throws IOException {
       out.writeInt(dataVersion);
    }

   private void readObject(java.io.ObjectInputStream in) throws IOException,
           ClassNotFoundException {
       int objDataVersion = in.readInt();
       if (objDataVersion == 1) {

       } else {
           throw new IOException("Can't handle dataversion: " + objDataVersion);   
       }

   }

   /**
    * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
    */
   public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
         throws TaskFailedException {
      try {
         Transaction t = worker.getActiveTransaction();
         t.abort();
      } catch (LeaseDeniedException e) {
         throw new TaskFailedException(e);
      } catch (RemoteException e) {
         throw new TaskFailedException(e);
      } catch (IOException e) {
         throw new TaskFailedException(e);
      } catch (InterruptedException e) {
         throw new TaskFailedException(e);
      } catch (PrivilegedActionException e) {
         throw new TaskFailedException(e);
      } catch (UnknownTransactionException e) {
         throw new TaskFailedException(e);
      } catch (CannotAbortException e) {
         throw new TaskFailedException(e);
      }
      return Condition.PROCESS_COMPLETE;
   }
   /**
    * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
    */
   public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
       //Nothing to do
       
   }

   /**
    * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
    */
   public Collection<Condition> getConditions() {
      return COMPLETE_CONDITION;
   }

   /**
    * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
    */
   public int[] getDataContainerIds() {
       return new int[] {  };
   }
}
