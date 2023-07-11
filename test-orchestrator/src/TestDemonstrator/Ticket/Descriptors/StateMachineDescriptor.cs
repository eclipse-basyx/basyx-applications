using System.ComponentModel.DataAnnotations;
using System.Xml.Serialization;
using TestDemonstrator.Ticket.Descriptors.Contracts;

namespace TestDemonstrator.Ticket.Descriptors
{
#if false
    [XmlType("properties")]
    public class Properties
    {
        [Required]
        [XmlAttribute(AttributeName = "submodel")]
        public string Submodel { get; set; }

        [Required]
        [XmlAttribute(AttributeName = "setter")]
        public string Setter { get; set; }

        [Required]
        [XmlAttribute(AttributeName = "getter")]
        public string Getter { get; set; }
    }

    [XmlType("transition")]
    public class Transition
    {
        [Required]
        [XmlAttribute(AttributeName = "event")]
        public string Event { get; set; }

        [Required]
        [XmlAttribute(AttributeName = "target")]
        public string Target { get; set; }
    }

    [XmlType("state")]
    public class State
    {
        [Required]
        [XmlElement(ElementName = "transition")]
        public Transition Transition { get; set; }

        [Required]
        [XmlAttribute(AttributeName = "id")]
        public string Id { get; set; }
    }

    [XmlType("path")]
    public class Path
    {
        [Required]
        [XmlElement(ElementName = "properties")]
        public Properties Properties { get; set; }

        [Required]
        [XmlElement(ElementName = "state")]
        public List<State> State { get; set; }
    }

    [XmlType("paths")]
    public class Paths
    {
        [Required]
        [XmlElement(ElementName = "path")]
        public List<Path> PathCollection { get; set; }
    }

    [XmlType("descriptor")]
    public class StateMachineDescriptor : IStateMachineDescriptor
    {
        [Required]
        [XmlElement(ElementName = "paths")]
        public Paths Paths { get; set; }
    }
#endif

    [XmlType("properties")]
    public class Properties
    {
        [Required]
        [XmlAttribute(AttributeName = "submodel")]
        public string? Submodel { get; set; }

        [Required]
        [XmlAttribute(AttributeName = "setter")]
        public string? Setter { get; set; }

        [Required]
        [XmlAttribute(AttributeName = "getter")]
        public string? Getter { get; set; }
    }

    [XmlType("transition")]
    public class Transition
    {
        [Required]
        [XmlAttribute(AttributeName = "event")]
        public string? Event { get; set; }

        [Required]
        [XmlAttribute(AttributeName = "target")]
        public string? Target { get; set; }

        [Required]
        [XmlAttribute(AttributeName = "wait")]
        public int Wait { get; set; }
    }

    [XmlType("state")]
    public class State
    {
        [Required]
        [XmlElement(ElementName = "transition")]
        public Transition? Transition { get; set; }

        [Required]
        [XmlAttribute(AttributeName = "id")]
        public string? Id { get; set; }
    }

    [XmlType("action")]
    public class Action
    {
        [Required]
        [XmlAttribute(AttributeName = "submodel")]
        public string? Submodel { get; set; }

        [Required]
        [XmlAttribute(AttributeName = "setter")]
        public string? Setter { get; set; }

        [Required]
        [XmlAttribute(AttributeName = "getter")]
        public string? Getter { get; set; }

        [Required]
        [XmlAttribute(AttributeName = "set")]
        public string? Set { get; set; }

        [Required]
        [XmlAttribute(AttributeName = "setType")]
        public string? SetType { get; set; }

        [Required]
        [XmlAttribute(AttributeName = "expect")]
        public string? Expect { get; set; }

        [Required]
        [XmlAttribute(AttributeName = "expectType")]
        public string? ExpectType { get; set; }

        [Required]
        [XmlAttribute(AttributeName = "wait")]
        public int Wait { get; set; }
    }

    [XmlType("onetimesetup")]
    public class Onetimesetup
    {
        [Required]
        [XmlElement(ElementName = "action")]
        public List<Action>? Actions { get; set; }
    }

    [XmlType("teardown")]
    public class Teardown
    {
        [Required]
        [XmlElement(ElementName = "action")]
        public List<Action>? Actions { get; set; }
    }

    [XmlType("setup")]
    public class Setup
    {
        [Required]
        [XmlElement(ElementName = "action")]
        public List<Action>? Actions { get; set; }
    }

    [XmlType("onetimeteardown")]
    public class Onetimeteardown
    {
        [Required]
        [XmlElement(ElementName = "action")]
        public List<Action>? Actions { get; set; }
    }

    [XmlType("path")]
    public class Path
    {
        [XmlElement(ElementName = "setup")]
        public Setup? Setup { get; set; }

        [Required]
        [XmlElement(ElementName = "properties")]
        public Properties? Properties { get; set; }

        [Required]
        [XmlElement(ElementName = "state")]
        public List<State>? State { get; set; }

        [XmlElement(ElementName = "teardown")]
        public Teardown? Teardown { get; set; }
    }

    [XmlType("paths")]
    public class Paths
    {
        [Required]
        [XmlElement(ElementName = "path")]
        public List<Path>? PathCollection { get; set; }
    }

    [XmlType("descriptor")]
    public class StateMachineDescriptor : IStateMachineDescriptor
    {
        [XmlElement(ElementName = "onetimesetup")]
        public Onetimesetup? Onetimesetup { get; set; }

        [Required]
        [XmlElement(ElementName = "paths")]
        public Paths? Paths { get; set; }

        [XmlElement(ElementName = "onetimeteardown")]
        public Onetimeteardown? Onetimeteardown { get; set; }
    }
}
