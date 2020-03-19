## What is a Content Type, a Content Object and a Property
A _**Content Type**_ defines the structure of a specific type of information which is produced, collected, managed, shared or published by an organization i.e. press releases, documents, bulletins, news, request forms, orders, blog entries, forum entries, persons, organizations, images, videos, etc. The Content Type does not contain the actual data. It just defines the set of **properties** that make up an arbitrary data structure. For example a person type defines that for each person we want to store a name, an address, phone numbers, interests, photos, accounts, etc. The characteristics of each property are determined by its **data type**. A property may have a **Simple Data Type** (i.e. integer, date, string), a **Complex Data Type** that defines nested properties, a **Reference to a Taxonomy Topic** or a **Reference to Content Object**.

A **Content Object** is an instance of a content type. Content Objects contain the actual data. For each content type many content objects / instances can be created. For example if a type for storing persons is defined, several person objects can be instantiated, each one storing different information for each person.

Through the ****Complex Data Type**** object properties, other properties can be arbitrarily nested inside and so each Content Object is actually a tree of properties. Additionally each content object can be connected with other objects through properties that their data type is "**Reference to Content Object"**. In this  way we end up to store an organization's information as a big graph of interconnected trees of properties.

Choosing appropriate terms for "things" that are stored in Astroboa have been always a good chance for debates. **Entity**, **Item** or simply an **Object** have been proposed as alternatives to **Content Object**. Also **Content Type** could be exchanged for **Content Class.** It seems that each user's background makes one term more appropriate than the other. Programmers of object oriented languages can immediately understand what an _entity class_ or _object class_ is. People less familiar with content management systems may find it more easy to understand that Astroboa can store data about different _Entities_. It holds also true that when you model an "Article" or a "Document" the term _Content Object_ seems pretty straightforward while the term _Entity_ seems more natural when a "person", an "organization" or a "species" is modeled, and _Item_ is quite easy to understand when "books", "machines" or any other artifact is modeled.

Throughout the documentation of Astroboa we will interchangeably use the terms _Content Object_ or simply _Object_ for instances and _Content Type_ or _Object Type_ for the defined types of structured data. The reader may think them as _Entities_ or _Items_ and _Classes_ respectively if the latter terms sound more familiar. In any case, the key idea is that any arbitrary type of information can be defined and stored.

## How a new Content Type is defined
For each _Content Type_ that is created the following are defined:

*   A set of standards-based Administrative Metadata (Dublin Core) that facilitate the management of Content Objects (title, description, subject, owner, author, publisher, etc.)
*   Core properties (creation date, summary, body text, binary channels, publication date, color, material, parts, price, etc) which constitute a certain type of information as percieved by the organization (i.e. an event, an order, a news item, a project, an inscription, etc.)
*   The type of information which is stored in each property (String, Date, Long, Binary Channel, Topic, References to other Content Object Instances, etc)
*   The minimum and maximum number of values for each property (cardinality)
*   Value ranges (enumerated values) for a property
*   The content type name (i.e. the system name for each type such as basicText, event, newsItem, etc.)
*   Localized labels for the property names, in any desired locale which can be used to present property names to a user according to her locale (i.e. provide localized entry forms)
*   Localized labels for the content type name which can be used to present the content type to a user according to her locale
*   Documentation about the way a content type may be used (i.e. document the type itself and provide help for content authors to facilitate choosing the most appropriate type for each case)
*   Documentation about the way each property may be used (i.e. provide help on how to fill values, of which values are allowed, etc.)

Astroboa allows a content modeler to define any number of _ContentObject Type_s and produce a continuously evolving organization's content model - an "open" content model as we like to call it. The user defines new types by means of XML Schema Files. Inside a schema file the user specifies an **XML Element** as an **extension** of a **core XML complex type** which is provided as part of the core Astroboa content model. The provided XML complex type after which all user types are defined and iherit its properties is called "**contentObjectType**". This core type along with other core Astroboa elements is as well defined inside an [XML Schema file]() which is stored inside the Astroboa content engine module (astroboa-repository module).

The following is an excerpt of the core Astroboa schema file that contains the definition of the contentObjectType, the parent type of all user defined content types:
```xml
<xs:complexType name="contentObjectType">
    <xs:complexContent>
        <xs:extension base="tns:AstroboaEntityType">
            <xs:sequence>
                <xs:element name="owner" type="tns:repositoryUserType" minOccurs="0"` maxOccurs="1"`/>
                <xs:element name="profile" minOccurs="1" maxOccurs="1" type="administrativeMetadataType:administrativeMetadataType"/>
                <xs:element name="accessibility" minOccurs="1" maxOccurs="1" type="accessibilityType:accessibilityType"/>
                <xs:element name="pageTemplateObjectReference" minOccurs="0" maxOccurs="1"` type="tns:contentObjectType" tns:acceptedContentTypes="pageTemplateObject">
                    <xs:annotation>
                        <xs:documentation xml:lang="en">
                            Reference to Page Template Object
                        </xs:documentation>
                        <xs:documentation xml:lang="el">
                            Αναφορά σε Αντικείμενο τύπου Πρότυπη Σελίδα
                        </xs:documentation>
                    </xs:annotation>
                </xs:element>
                <xs:element name="pageTemplate" type="tns:topicType" minOccurs="0"` maxOccurs="1">
                      <xs:annotation>
                          <xs:documentation xml:lang="en">
                              Page Template
                          </xs:documentation>
                      <xs:documentation xml:lang="el">
                          Πρότυπη Σελίδα
                      </xs:documentation>
                      </xs:annotation>
                </xs:element>
            </xs:sequence>
            <!-- A human readable system name identifying this content object used -->
            <xs:attribute name="contentObjectTypeName" use="optional" type="xs:string"/>
            <xs:attribute name="systemName" use="optional" type="tns:systemNameType"/>
        </xs:extension>
    </xs:complexContent>
</xs:complexType>
```

All content types in Astroboa must extend the above XML complex type. Doing so, they inherit the basic properties required by the content engine. More specifically, a_n Object_ of any type automatically possesses the following properties:

*   _**cmsIdentifier**_ A PERMANENT UNIQUE identifier for each content object instance in the form of a [UUID](https://web.archive.org/web/20110829231136/http://en.wikipedia.org/wiki/Uuid). This attribute is defined in XML complex type **_AstroboaEntityType_** located in Astroboa-model-types-\[Astroboa version\].xsd. Astroboa automatically generates a value for this attribute upon creation of a new object instance. This identifies as permanent and cannot be changed.

*   _**systemName**_ A human readable NON-PERMANENT UNIQUE identifier for each content object instance. This identifier is useful for generating user / SEO friendly [Resource API](/web/20110829231136/http://www.astroboa.org/portal/resource/section/astroboa-resource-api) URLs that will be used for sharing your object instances to the web (mashups) or presenting them to web pages.  
    For example:

`http://mysite/section/open-content-model`

is more friendly to users and search engines than the following URL that uses the UUID to access the content of an object:

`http://mysite/section/ba49f0d4-247b-40f2-9db5-f2ab0f4b95d0`

*   _**contentObjectTypeName**_ This property holds the content type of an object instance, e.g. "personObject", "organizationObject", etc. It does not store the whole type definition but just the name of the type as it is defined in the relevant XML Schema.

*   _**owner**_ A reference to the [RepositoryUser](content-modeling-repository-user.md) who owns the _Object instance_.

*   **_pageTemplateObject_** A reference to a _pageTemplateObject_ instance. Page template objects are used to hold templates for rendering _Objects_ to web pages or other display technologies. For example the page template code stored inside the pageTemplateObject could be a facelets xhtml page or a PHP template page or an xslt for rendering pdfs. You may use this property if you require to store display templates inside Astroboa. If you want to just provide a symbolic name for the required template and let each content application to decide how to interpret the symbolic name then you may use the other default property which is called 'pageTemplate' and is of type [Topic](content-modeling-topic.md). Refer to [Astroboa Portal Commons Library](the-portal-commons-library.md) for more detailed info.

*   _**pageTemplate**_ The symbolic name (provided through a Astroboa vocabulary) of the page template to be used when presenting instances of this content type. The symbolic name can be interpreted and mapped differently by different content applications. According to the templating engine that the content application utilizes the appropriate real template should be located by means of the symbolic name. For example, assuming a web site that utilizes Facelets+JSF the name may correspond to an xhtm faces page template. Or if the engine supports XML transformations then the content object may be consumed in XML form and then the template should correspond to an approprite XSLT that transforms the XML to PDF or HTML. Refer to [Astroboa Portal Commons Library](the-portal-commons-library.md) for more detailed info.

*   _**profile**_ This complex property stores administrative metadata according to the Dublin Core standard. It is chosen not to use the original Dublin Core namespaces and definitions in order to allow a more flexible management of these metadata utilizing the extra facilities provided by Astroboa, i.e. to use [Topics](content-modeling-topic.md) for Subject instead of plain strings or to use enumerations for language and status instead of plain fields. Administrative Metadata is mandatory for every _Object instance._ By convention every _Object has_ a field called "_**profile**_**"** and the type of the field is [_**administrativeMetadataType**](). In contrast to the convention of naming fields after their type name (in this case the field name should be _**administrativeMetadata**_), it has been chosen to name the fields of this type with the more abstract name _**profile**_. _**Profile**_, as the name of the complex field that holds the basic information of an _Object_ is more user friendly for programmers which may not be aware of CMS terms or the Dublin Core standard. So the current requirement is that every _Object_ has a _**profile**_ property (complex property) that holds administrative metadata. Many of the administrative metadata such as the '_Title_', the '_creation and modification dates_', etc. are mandatory for proper functioning of the Astroboa content engine and/or the Astroboa Web Console. 

*   _**accessibility**_  This complex property stores access right lists for each _Object instance._ It defines four sub properties (**_canBeReadBy_**, _**canBeUpdatedBy**_, _**canBeDeletedBy**_, _**canBeTaggedBy**_) whose values correspond to individual Repository Users or User Groups. For More information about this check Astroboa Security. By convention every _Object Type inherits the_ _**accessibility**_ property and the type of this property is [_**accessibilityType**_]().

## Defining a content type in Astroboa using XML Schema
An _Object_ type has one or more properties. A property can either be a complex property, that is, a property which contains other properties or a simple property, that is, a property whose values are simple types like strings, dates, longs, doubles, etc. _Content Object_ types and their properties can be described by a code developer or a content modeler using an XML Schema and following some very simple rules. These rules are presented in the following example which describes how to define content type '_blogEntry_' in Astroboa in an XML schema.

In a _blog entry_ one is expected to find a _title_, may be a _summary_, the _body_ of the blog entry and the _day of blog entry publication_. A _blog entry_ contains also one or more _comments_. A _comment_, on the other hand, apart from its _body_, it is expected to contain a value identifying the user who made the comment and a _day of comment publication_. Finally a _blog entry_ may have one or more _file attachments_ and it may be categorized with one or more tags.

So, roughly, the following properties constitute a _blog entry_:

*   _Title_, simple property whose single value is plain text
*   _Summary_, simple property whose single value is plain text
*   _Body_, simple property whose single value is rich text
*   _Day of publication_, simple property whose single value is a date
*   One or more _comments_. _Comment_ is a complex property and thus defines some properties on its own
    *   _Body_, simple property whose single value is rich text
    *   _UserId_, simple property whose value is plain text
    *   _Day of publication_, simple property whose single value is a date
*   One or more _file attachments_ whose type is binary.
*   One or more user tags whose type is [Topic](content-modeling-topic.md).

In order to define a content type in Astroboa, content modeler/developer should create a global XML element and have it extend XML complex type **_contentObjectType_**. What follows is the XML Schema describing an Astroboa content type, named '_blogEntryObject_':
```xml
<xs:element name="taxonomy" type="tns:taxonomyType"/>
<xs:complexType name="taxonomyType" final="#all">
    <xs:complexContent> 
        <xs:extension base="tns:localizableBetaCmsEntityType"> 
            <xs:sequence> 
                <!--  Root topics -->
                <xs:element name="rootTopics" minOccurs="0" maxOccurs="1">
                    <xs:complexType> 
                        <xs:sequence>
                            <xs:element name="topic" type="tns:topicType" minOccurs="0" maxOccurs="unbounded"/>
                        </xs:sequence>
                    </xs:complexType>
                </xs:element>
            </xs:sequence>
            <xs:attribute name="name" use="optional">
                <xs:simpleType>
                    <xs:union memberTypes="xs:NCName">
                        <xs:simpleType>
                            <xs:restriction base="xs:string">
                                <xs:enumeration value="bccms:subjectTaxonomy"/>
                                <xs:enumeration value="bccms:folksonomy"/>
                            </xs:restriction>
                        </xs:simpleType>
                    </xs:union>
                </xs:simpleType>
            </xs:attribute>
        </xs:extension>
    </xs:complexContent>
</xs:complexType> 
</xs:extension>
</xs:complexContent>
 </xs:complexType>
 </xs:element>
 </xs:schema>
```

Astroboa interprets above XML Schema as a definition for a content type named '_blogEntryObject_'. One could argue that content type's name could well be '_blogEntry_' but we tend to suffix content type names with '_Object_' in order to distinguish them from other XML element definitions which do not represent content types and to quickly denote that this element represents a content type. A content modeler/developer however, is free to apply her own naming conventions.

Above XML Schema also declares two labels for content type, one for Greek Locale and another one for English locale. XML **_any_** tag must always exist in content type definition for reasons which are well explained in section for adding aspects to _Content Object_.

By extending base complex type _**bccmsmodel:contentObjectType, 'blogEntryObject'**_ inherits all properties mentioned in the above section. Without moving any further, several properties have already been defined for our content type.
