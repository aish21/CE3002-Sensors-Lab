package javax.mail;

import java.util.EventListener;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import javax.mail.event.MailEvent;

class EventQueue implements Runnable {
    private static WeakHashMap<ClassLoader, EventQueue> appq;
    private Executor executor;

    /* renamed from: q */
    private volatile BlockingQueue<QueueElement> f295q;

    static class TerminatorEvent extends MailEvent {
        private static final long serialVersionUID = -2481895000841664111L;

        TerminatorEvent() {
            super(new Object());
        }

        public void dispatch(Object listener) {
            Thread.currentThread().interrupt();
        }
    }

    static class QueueElement {
        MailEvent event = null;
        Vector<? extends EventListener> vector = null;

        QueueElement(MailEvent event2, Vector<? extends EventListener> vector2) {
            this.event = event2;
            this.vector = vector2;
        }
    }

    EventQueue(Executor ex) {
        this.executor = ex;
    }

    /* access modifiers changed from: package-private */
    public synchronized void enqueue(MailEvent event, Vector<? extends EventListener> vector) {
        if (this.f295q == null) {
            this.f295q = new LinkedBlockingQueue();
            if (this.executor != null) {
                this.executor.execute(this);
            } else {
                Thread qThread = new Thread(this, "Jakarta-Mail-EventQueue");
                qThread.setDaemon(true);
                qThread.start();
            }
        }
        this.f295q.add(new QueueElement(event, vector));
    }

    /* access modifiers changed from: package-private */
    public synchronized void terminateQueue() {
        if (this.f295q != null) {
            Vector<EventListener> dummyListeners = new Vector<>();
            dummyListeners.setSize(1);
            this.f295q.add(new QueueElement(new TerminatorEvent(), dummyListeners));
            this.f295q = null;
        }
    }

    static synchronized EventQueue getApplicationEventQueue(Executor ex) {
        EventQueue q;
        synchronized (EventQueue.class) {
            ClassLoader cl = Session.getContextClassLoader();
            if (appq == null) {
                appq = new WeakHashMap<>();
            }
            q = appq.get(cl);
            if (q == null) {
                q = new EventQueue(ex);
                appq.put(cl, q);
            }
        }
        return q;
    }

    public void run() {
        BlockingQueue<QueueElement> bq = this.f295q;
        if (bq != null) {
            while (true) {
                try {
                    QueueElement qe = bq.take();
                    MailEvent e = qe.event;
                    Vector<? extends EventListener> v = qe.vector;
                    for (int i = 0; i < v.size(); i++) {
                        try {
                            e.dispatch(v.elementAt(i));
                        } catch (Throwable t) {
                            if (t instanceof InterruptedException) {
                                return;
                            }
                        }
                    }
                } catch (InterruptedException e2) {
                    return;
                }
            }
        }
    }
}
