using BaSyx.Models.Core.AssetAdministrationShell.Implementations;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace AAS_Generator
{
    public class RandomStringProperty : Property<string>
    {
        List<string> vals;
        public RandomStringProperty(string idShort, List<string> vals) : base(idShort)
        {

            this.vals = vals;
            int n = vals.Count;
            Random rnd = new Random();

            this.Get = p =>
            {
                int nextIdx = rnd.Next(n);
                return vals[nextIdx];
            };

            this.Set = (p, val) =>
            {
                Console.WriteLine("Received Set: " + val);
            };
        }
    }
}
