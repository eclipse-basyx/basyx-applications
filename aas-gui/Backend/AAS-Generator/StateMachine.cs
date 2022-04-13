using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using BaSyx.Models.Core.AssetAdministrationShell.Implementations;
using System.Threading; 
namespace AAS_Generator
{
    public class StateMachine : SubmodelElementCollection
    {
        enum State : ushort
        {
        undefined = 0,
        clearing = 1,
        stopped = 2,
        starting = 3,
        idle = 4,
        suspended = 5,
        execute = 6,
        stopping = 7,
        aborting = 8,
        aborted = 9,
        holding = 10,
        held = 11,
        unholding = 12,
        suspending = 13,
        unsuspending = 14,
        resetting = 15,
        completing = 16,
        complete = 17
        };

        public enum Command : ushort  
        {
            undefined = 0,
            reset = 1,
            start = 2,
            stop = 3,
            hold = 4,
            unhold = 5,
            suspend = 6,
            unsuspend = 7,
            abort = 8,
            clear = 9,
            sc = 10
        };

        public enum Mode : ushort
        {
            production = 1,
            maintenance = 2,
            manual = 3
        }

        Dictionary<ushort,string> stateLookup = new Dictionary<ushort, string>
        {
            {0, "undefined"},
            {1, "clearing"},
            {2, "stopped" },
            {3, "starting" },
            {4, "idle" },
            {5, "suspended" },
            {6, "execute" },
            {7, "stopping" },
            {8, "aborting" },
            {9, "aborted" },
            {10, "holding" },
            {11, "held" },
            {12, "unholding" },
            {13, "suspending" },
            {14, "unsuspending" },
            {15, "resetting" },
            {16, "completing" },
            {17, "complete" }
        };

        Dictionary<string, ushort> cmdLookup = new Dictionary<string, ushort>
        {
            {"undefined", 0 },
            {"reset", 1 },
            {"start", 2 },
            {"stop", 3},
            {"hold", 4},
            {"unhold", 5},
            {"suspend", 6},
            {"unsuspend", 7},
            {"abort", 8 },
            {"clear", 9 }
        };

        Dictionary<string, ushort> modeLookup = new Dictionary<string, ushort>
        {
            {"production", 1 },
            {"maintenance", 2 },
            {"manual", 3},
        };

        Dictionary<ushort, string> modeReverseLookup = new Dictionary<ushort, string>
        {
            {1, "production"},
            {2, "maintenance"},
            {3, "manual"},
        };

        Dictionary<(State,Command), State> transitionTable = new Dictionary<(State, Command), State>
        {
            {(State.clearing, Command.abort), State.aborting},
            {(State.starting, Command.abort), State.aborting},
            {(State.unholding, Command.abort), State.aborting},
            {(State.holding, Command.abort), State.aborting},
            {(State.undefined, Command.abort), State.aborting},
            {(State.stopped, Command.abort), State.aborting},
            {(State.suspending, Command.abort), State.aborting},
            {(State.resetting, Command.abort), State.aborting},
            {(State.unsuspending, Command.abort), State.aborting},
            {(State.completing, Command.abort), State.aborting},
            {(State.suspended, Command.abort), State.aborting},
            {(State.idle, Command.abort), State.aborting},
            {(State.held, Command.abort), State.aborting},
            {(State.stopping, Command.abort), State.aborting},
            {(State.complete, Command.abort), State.aborting},
            {(State.execute, Command.abort), State.aborting},
            {(State.starting, Command.stop), State.stopping},
            {(State.unholding, Command.stop), State.stopping},
            {(State.holding, Command.stop), State.stopping},
            {(State.undefined, Command.stop), State.stopping},
            {(State.suspending, Command.stop), State.stopping},
            {(State.resetting, Command.stop), State.stopping},
            {(State.unsuspending, Command.stop), State.stopping},
            {(State.completing, Command.stop), State.stopping},
            {(State.suspended, Command.stop), State.stopping},
            {(State.idle, Command.stop), State.stopping},
            {(State.held, Command.stop), State.stopping},
            {(State.complete, Command.stop), State.stopping},
            {(State.execute, Command.stop), State.stopping},
            {(State.aborting, Command.sc), State.aborted},
            {(State.clearing, Command.sc), State.stopped},
            {(State.stopping, Command.sc), State.stopped},
            {(State.resetting, Command.sc), State.idle},
            {(State.starting, Command.sc), State.execute},
            {(State.execute, Command.sc), State.completing},
            {(State.completing, Command.sc), State.complete},
            {(State.suspending, Command.sc), State.suspended},
            {(State.unsuspending, Command.sc), State.execute},
            {(State.unholding, Command.sc), State.execute},
            {(State.holding, Command.sc), State.held},
            {(State.aborted, Command.clear), State.clearing},
            {(State.stopped, Command.reset), State.resetting},
            {(State.idle, Command.start), State.starting},
            {(State.execute, Command.suspend), State.suspending},
            {(State.suspended, Command.unsuspend), State.unsuspending},
            {(State.held, Command.unhold), State.unholding},
            {(State.complete, Command.reset), State.resetting},
        };

        HashSet<State> scSet = new HashSet<State>
        {   State.aborting,
            State.clearing,
            State.stopping,
            State.resetting,
            State.starting,
            State.execute,
            State.completing,
            State.suspending,
            State.unsuspending,
            State.unholding,
            State.holding};


        State state;
        Mode mode;
        bool isRandom;

        public StateMachine(string idShort, bool isRandom=false) : base(idShort)
        {
            state = State.aborted;
            this.isRandom = isRandom;

            var stateProp = new Property<string>("State")
            {
                Get = prop => { return stateLookup[(ushort) state]; }
            };

            var cmdProp = new Property<string>("Command")
            {
                Set = (prop, val) => { transition(val); }
            };


            var modeProp = new Property<string>("Mode")
            {
                Get = prop => { return "production"; },
                Set = (prop, val) => { }
            };

            Value.Add(stateProp);
            Value.Add(cmdProp);
            Value.Add(modeProp);

            if(isRandom)
            {
                Thread th = new Thread(randomTransition);
                th.Start();
            }

        }

        public void randomTransition()
        {
            while(true)
            { 
                Thread.Sleep(8000);
                LinkedList<Command> cmds = new LinkedList<Command>();
                var rand = new Random();
                foreach(var entry in transitionTable)
                {
                    var key = entry.Key;
                    var val = entry.Value;

                    if(key.Item1 == state)
                    {
                        cmds.AddLast(key.Item2);
                    }
                }

                var arr = cmds.ToArray();
                int idx = rand.Next(arr.Length);
                transition(arr[idx]);
            }
        }

        public void transition(string cmd)
        {
            Command realCmd = (Command) cmdLookup[cmd];
            transition(realCmd);
        }

        public void transition(Command cmd)
        {
            Thread.Sleep(2500);
            state = transitionTable[(state, cmd)];

            if (scSet.Contains(state))
                transition(Command.sc);
        }
    }
}
