//Libraries
using System.ComponentModel.DataAnnotations;
using System.Text;
using System.Xml;
using System.Xml.Schema;
using System.Xml.Serialization;

using BaSyx.Models.Core.AssetAdministrationShell.Generics;
using BaSyx.Models.Export;

//Core
using TestDemonstrator.Ticket.Contracts;
using TestDemonstrator.Ticket.Descriptors;
using TestDemonstrator.Ticket.Descriptors.Contracts;

namespace TestDemonstrator.Ticket
{
    /// <summary>
    /// The following class serves as a template for the instantiation of concrete instances
    /// of the <see cref="IDescriptorFactory"/> interface
    /// </summary>
    public class DescriptorFactory : IDescriptorFactory
    {
        /// <inheritdoc/>
        public string CreatePassiveShell(IAssetAdministrationShell descriptor)
        {
            var env = new AssetAdministrationShellEnvironment_V2_0(descriptor);
            var serializer = new XmlSerializer(typeof(AssetAdministrationShellEnvironment_V2_0), "http://www.admin-shell.io/aas/2/0");
            var xmlNamespaces = new XmlSerializerNamespaces();
            xmlNamespaces.Add("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            xmlNamespaces.Add("aas", "http://www.admin-shell.io/aas/2/0");
            xmlNamespaces.Add("IEC61360", "http://www.admin-shell.io/IEC61360/2/0");
            using StringWriter textWriter = new StringWriter();
            serializer.Serialize(textWriter, env, xmlNamespaces);
            return textWriter.ToString();
        }

        /// <inheritdoc/>
        public IAssetAdministrationShell CreatePassiveShell(string content)
        {
            IAssetAdministrationShell aas = null!;
            byte[] contentBytes = Encoding.Unicode.GetBytes(content);
            using Stream stream = new MemoryStream(contentBytes);
            var env = AssetAdministrationShellEnvironment_V2_0.ReadEnvironment_V2_0(stream, ExportType.Xml);
            aas = env.AssetAdministrationShells.First();
            return aas;
        }

        /// <inheritdoc/>
        public string CreateStructureDescriptor(IAssetAdministrationShell descriptor)
        {
            var env = new AssetAdministrationShellEnvironment_V2_0(descriptor);
            var serializer = new XmlSerializer(typeof(AssetAdministrationShellEnvironment_V2_0), "http://www.admin-shell.io/aas/2/0");
            var xmlNamespaces = new XmlSerializerNamespaces();
            xmlNamespaces.Add("xsi", "http://www.w3.org/2001/XMLSchema-instance");
            xmlNamespaces.Add("aas", "http://www.admin-shell.io/aas/2/0");
            xmlNamespaces.Add("IEC61360", "http://www.admin-shell.io/IEC61360/2/0");
            using StringWriter textWriter = new StringWriter();
            serializer.Serialize(textWriter, env, xmlNamespaces);
            return textWriter.ToString();
        }

        /// <inheritdoc/>
        public IStructureDescriptor CreateStructureDescriptor(string content)
        {
            IAssetAdministrationShell aas = null!;
            AssetAdministrationShellEnvironment_V2_0 env = null!;

            byte[] contentBytes = Encoding.Unicode.GetBytes(content);
            using Stream stream = new MemoryStream(contentBytes);
            env = AssetAdministrationShellEnvironment_V2_0.ReadEnvironment_V2_0(stream, ExportType.Xml);

            aas = env.AssetAdministrationShells.First();
            return new StructureDescriptor(aas);
        }

        /// <inheritdoc/>
        public string CreateStateMachineDescriptor(IStateMachineDescriptor descriptor)
        {
            if (descriptor == null || descriptor.Paths == null)
            {
                throw new InvalidOperationException("Unserializable structure descriptor!");
            }

            var serializer = new XmlSerializer(typeof(StateMachineDescriptor));
            using var writer = new StringWriter();
            serializer.Serialize(writer, descriptor);
            return writer.ToString();
        }

        /// <inheritdoc/>
        public IStateMachineDescriptor CreateStateMachineDescriptor(string content)
        {
            XmlReaderSettings settings = new XmlReaderSettings();
            settings.ValidationType = ValidationType.Schema;
            settings.ValidationFlags |= XmlSchemaValidationFlags.ProcessInlineSchema;
            settings.ValidationFlags |= XmlSchemaValidationFlags.ProcessSchemaLocation;
            settings.ValidationFlags |= XmlSchemaValidationFlags.ReportValidationWarnings;
            settings.ValidationEventHandler += new ValidationEventHandler(StateMachineDescriptorValidationCallBack!);


            if (File.Exists("StateMachineDescriptorXsd.xsd"))
            {
                settings.Schemas.Add(null, "StateMachineDescriptorXsd.xsd");
            }
            else if (File.Exists("../../../StateMachineDescriptorXsd.xsd"))
            {
                settings.Schemas.Add(null, "../../../StateMachineDescriptorXsd.xsd");
            }
            else if (File.Exists("TestDemonstratorAAS/StateMachineDescriptorXsd.xsd"))
            {
                settings.Schemas.Add(null, "TestDemonstratorAAS/StateMachineDescriptorXsd.xsd");
            }
            else
            {
                throw new InvalidOperationException($"XML state machine descriptor schema StateMachineDescriptorXsd.xsd not found in {Directory.GetCurrentDirectory()}!");
            }

            byte[] contentBytes = Encoding.Unicode.GetBytes(content);
            using Stream stream = new MemoryStream(contentBytes);

            XmlSerializer xmlSerializer = new XmlSerializer(typeof(StateMachineDescriptor), new XmlRootAttribute("descriptor"));
            using XmlReader xmlReader = XmlReader.Create(stream, settings);
            var descriptor = (StateMachineDescriptor?)xmlSerializer.Deserialize(xmlReader);

            if (descriptor == null || !IsValid(descriptor))
            {
                throw new InvalidOperationException("Invalid structure descriptor detected!");
            }

            return descriptor;
        }

        private static bool IsValid(object dto)
        {
            var validationContext = new ValidationContext(dto);
            var validationResult = new List<ValidationResult>();
            var returnValue = Validator.TryValidateObject(dto, validationContext, validationResult, true);

            return returnValue;
        }

        private static void StateMachineDescriptorValidationCallBack(object sender, ValidationEventArgs args)
        {
            string message;

            if (args.Severity == XmlSeverityType.Warning)
                message = $"Validation warning: {args.Message}";
            else
                message = 
                    $"Validation error: {args.Message} " +
                    $"| LineNumber: {args.Exception.LineNumber}  " +
                    $"| LinePosition: {args.Exception.LinePosition}";

            throw new InvalidOperationException(message);
        }
    }
}
