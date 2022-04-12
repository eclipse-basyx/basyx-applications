using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using BaSyx.Models.Core.AssetAdministrationShell.Implementations;
namespace AAS_Generator
{
    public class StaticProperty<T> : Property<T>
    {
        public StaticProperty(string idShort, T Value) : base(idShort)
        {
            this.Value = Value;

            this.Get = p =>
            {
                return this.Value;
            };

            this.Set = (p, val) =>
            {
                this.Value = val;
            };
        }


    }
}
