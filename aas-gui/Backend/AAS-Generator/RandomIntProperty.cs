using BaSyx.Models.Core.AssetAdministrationShell.Implementations;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace AAS_Generator
{
    public class RandomIntProperty : Property<int>
    {
        public RandomIntProperty(string idShort) : base(idShort)
        {
            Random rnd = new Random();

            this.Get = p =>
            {
                return rnd.Next(101);
            };

            this.Set = (p, val) =>
            {
                Console.WriteLine("Received Set: " + val);
            };
        }
    }
}
