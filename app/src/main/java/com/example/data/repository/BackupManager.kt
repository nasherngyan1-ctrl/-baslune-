package com.example.data.repository

import com.example.data.local.*
import com.example.domain.model.*
import org.json.JSONArray
import org.json.JSONObject

object BackupManager {
    const val FORMAT_IDENTIFIER = "BASLUNE_OS_BACKUP"
    const val SCHEMA_VERSION = 1

    sealed class ValidationResult {
        data class Valid(
            val snapshot: DatabaseSnapshot,
            val exportedAt: Long,
            val includedModules: List<String>
        ) : ValidationResult()

        data class Invalid(val reason: String) : ValidationResult()
    }

    fun exportToJson(
        snapshot: DatabaseSnapshot,
        selectedModules: Set<AppModule>? = null,
        exportedAt: Long = System.currentTimeMillis()
    ): String {
        val includeAll = selectedModules == null || selectedModules.isEmpty()
        fun has(module: AppModule) = includeAll || selectedModules!!.contains(module)

        val root = JSONObject().apply {
            put("format", FORMAT_IDENTIFIER)
            put("schemaVersion", SCHEMA_VERSION)
            put("exportedAt", exportedAt)
            put("modules", JSONArray((selectedModules ?: AppModule.entries.toSet()).map { it.name }))

            val data = JSONObject()
            if (has(AppModule.TEAM)) {
                data.put("teamMembers", JSONArray().apply {
                    snapshot.teamMembers.forEach { m ->
                        put(JSONObject().apply {
                            put("id", m.id); put("name", m.name); put("email", m.email)
                            put("role", m.role.name); put("department", m.department)
                            put("isActive", m.isActive); put("createdAt", m.createdAt)
                        })
                    }
                })
            }
            if (has(AppModule.CLIENTS)) {
                data.put("clients", JSONArray().apply {
                    snapshot.clients.forEach { c ->
                        put(JSONObject().apply {
                            put("id", c.id); put("clientCode", c.clientCode); put("fullName", c.fullName)
                            put("companyName", c.companyName); put("email", c.email); put("phone", c.phone)
                            put("whatsapp", c.whatsapp); put("country", c.country); put("city", c.city)
                            put("address", c.address); put("clientType", c.clientType.name); put("industry", c.industry)
                            put("acquisitionSource", c.acquisitionSource); put("dateAdded", c.dateAdded)
                            put("status", c.status.name); put("notes", c.notes); put("tags", c.tags)
                            put("isSample", c.isSample); put("isArchived", c.isArchived)
                        })
                    }
                })
                data.put("communications", JSONArray().apply {
                    snapshot.communications.forEach { c ->
                        put(JSONObject().apply {
                            put("id", c.id); put("clientId", c.clientId); put("interactionType", c.interactionType)
                            put("summary", c.summary); put("requirements", c.requirements)
                            put("followUpDate", c.followUpDate ?: JSONObject.NULL)
                            put("date", c.date); put("isSample", c.isSample)
                        })
                    }
                })
            }
            if (has(AppModule.PROJECTS)) {
                data.put("projects", JSONArray().apply {
                    snapshot.projects.forEach { p ->
                        put(JSONObject().apply {
                            put("id", p.id); put("projectCode", p.projectCode); put("name", p.name)
                            put("clientId", p.clientId ?: JSONObject.NULL); put("description", p.description)
                            put("category", p.category.name); put("startDate", p.startDate)
                            put("deadline", p.deadline ?: JSONObject.NULL); put("budget", p.budget)
                            put("agreedPrice", p.agreedPrice); put("currency", p.currency.name)
                            put("status", p.status.name); put("priority", p.priority.name)
                            put("assignedMembers", p.assignedMembers); put("progressPercent", p.progressPercent)
                            put("deliverables", p.deliverables); put("externalUrl", p.externalUrl)
                            put("classification", p.classification); put("internalNotes", p.internalNotes)
                            put("isStarter", p.isStarter); put("isSample", p.isSample)
                            put("isArchived", p.isArchived); put("updatedAt", p.updatedAt)
                        })
                    }
                })
                data.put("milestones", JSONArray().apply {
                    snapshot.milestones.forEach { m ->
                        put(JSONObject().apply {
                            put("id", m.id); put("projectId", m.projectId); put("title", m.title)
                            put("dueDate", m.dueDate ?: JSONObject.NULL); put("isCompleted", m.isCompleted)
                            put("isSample", m.isSample)
                        })
                    }
                })
            }
            if (has(AppModule.TASKS)) {
                data.put("tasks", JSONArray().apply {
                    snapshot.tasks.forEach { t ->
                        put(JSONObject().apply {
                            put("id", t.id); put("title", t.title); put("description", t.description)
                            put("projectId", t.projectId ?: JSONObject.NULL); put("clientId", t.clientId ?: JSONObject.NULL)
                            put("category", t.category); put("priority", t.priority.name); put("status", t.status.name)
                            put("startDate", t.startDate); put("dueDate", t.dueDate ?: JSONObject.NULL)
                            put("estimatedHours", t.estimatedHours); put("actualHours", t.actualHours)
                            put("assignedTo", t.assignedTo); put("notes", t.notes)
                            put("completionDate", t.completionDate ?: JSONObject.NULL)
                            put("isRecurring", t.isRecurring); put("recurrenceLabel", t.recurrenceLabel)
                            put("isSample", t.isSample)
                        })
                    }
                })
            }
            if (has(AppModule.SERVICES)) {
                data.put("services", JSONArray().apply {
                    snapshot.services.forEach { s ->
                        put(JSONObject().apply {
                            put("id", s.id); put("name", s.name); put("description", s.description)
                            put("category", s.category.name); put("priceType", s.priceType.name)
                            put("startingPrice", s.startingPrice); put("currency", s.currency.name)
                            put("estimatedDelivery", s.estimatedDelivery); put("requiredInfo", s.requiredInfo)
                            put("includedFeatures", s.includedFeatures); put("optionalAddOns", s.optionalAddOns)
                            put("isActive", s.isActive); put("isSample", s.isSample)
                        })
                    }
                })
            }
            if (has(AppModule.SALES)) {
                data.put("opportunities", JSONArray().apply {
                    snapshot.opportunities.forEach { o ->
                        put(JSONObject().apply {
                            put("id", o.id); put("title", o.title); put("potentialClientName", o.potentialClientName)
                            put("clientId", o.clientId ?: JSONObject.NULL)
                            put("requestedServiceId", o.requestedServiceId ?: JSONObject.NULL)
                            put("requestedServiceName", o.requestedServiceName)
                            put("estimatedValue", o.estimatedValue); put("currency", o.currency.name)
                            put("expectedClosingDate", o.expectedClosingDate ?: JSONObject.NULL)
                            put("source", o.source); put("stage", o.stage.name); put("probability", o.probability)
                            put("notes", o.notes); put("nextFollowUpDate", o.nextFollowUpDate ?: JSONObject.NULL)
                            put("communicationNotes", o.communicationNotes)
                            put("convertedClientId", o.convertedClientId ?: JSONObject.NULL)
                            put("convertedProjectId", o.convertedProjectId ?: JSONObject.NULL)
                            put("isSample", o.isSample); put("updatedAt", o.updatedAt)
                        })
                    }
                })
            }
            if (has(AppModule.INVOICES)) {
                data.put("invoices", JSONArray().apply {
                    snapshot.invoices.forEach { i ->
                        put(JSONObject().apply {
                            put("id", i.id); put("invoiceNumber", i.invoiceNumber)
                            put("clientId", i.clientId ?: JSONObject.NULL); put("clientNameSnapshot", i.clientNameSnapshot)
                            put("projectId", i.projectId ?: JSONObject.NULL); put("issueDate", i.issueDate)
                            put("dueDate", i.dueDate); put("currency", i.currency.name); put("subtotal", i.subtotal)
                            put("discount", i.discount); put("tax", i.tax); put("total", i.total)
                            put("paidAmount", i.paidAmount); put("status", i.status.name)
                            put("notes", i.notes); put("isSample", i.isSample)
                        })
                    }
                })
                data.put("invoiceItems", JSONArray().apply {
                    snapshot.invoiceItems.forEach { it ->
                        put(JSONObject().apply {
                            put("id", it.id); put("invoiceId", it.invoiceId)
                            put("serviceId", it.serviceId ?: JSONObject.NULL); put("title", it.title)
                            put("quantity", it.quantity); put("unitPrice", it.unitPrice); put("lineTotal", it.lineTotal)
                        })
                    }
                })
                data.put("payments", JSONArray().apply {
                    snapshot.payments.forEach { p ->
                        put(JSONObject().apply {
                            put("id", p.id); put("invoiceId", p.invoiceId); put("amount", p.amount)
                            put("currency", p.currency.name); put("paymentDate", p.paymentDate)
                            put("method", p.method.name); put("notes", p.notes); put("isSample", p.isSample)
                        })
                    }
                })
            }
            if (has(AppModule.FINANCE)) {
                data.put("transactions", JSONArray().apply {
                    snapshot.transactions.forEach { tx ->
                        put(JSONObject().apply {
                            put("id", tx.id); put("transactionCode", tx.transactionCode); put("type", tx.type.name)
                            put("amount", tx.amount); put("currency", tx.currency.name); put("category", tx.category.name)
                            put("date", tx.date); put("paymentMethod", tx.paymentMethod.name)
                            put("clientId", tx.clientId ?: JSONObject.NULL); put("projectId", tx.projectId ?: JSONObject.NULL)
                            put("linkedInvoiceId", tx.linkedInvoiceId ?: JSONObject.NULL)
                            put("linkedPaymentId", tx.linkedPaymentId ?: JSONObject.NULL)
                            put("description", tx.description); put("receiptUrl", tx.receiptUrl)
                            put("status", tx.status.name); put("isSample", tx.isSample); put("updatedAt", tx.updatedAt)
                        })
                    }
                })
            }
            if (has(AppModule.MARKETING)) {
                data.put("campaigns", JSONArray().apply {
                    snapshot.campaigns.forEach { c ->
                        put(JSONObject().apply {
                            put("id", c.id); put("name", c.name); put("channel", c.channel.name)
                            put("status", c.status.name); put("startDate", c.startDate)
                            put("endDate", c.endDate ?: JSONObject.NULL); put("adSpend", c.adSpend)
                            put("currency", c.currency.name); put("leadsGenerated", c.leadsGenerated)
                            put("notes", c.notes); put("isSample", c.isSample)
                        })
                    }
                })
                data.put("contentItems", JSONArray().apply {
                    snapshot.contentItems.forEach { c ->
                        put(JSONObject().apply {
                            put("id", c.id); put("title", c.title); put("platform", c.platform.name)
                            put("contentType", c.contentType); put("caption", c.caption)
                            put("plannedDate", c.plannedDate); put("status", c.status.name)
                            put("mediaLink", c.mediaLink); put("campaignId", c.campaignId ?: JSONObject.NULL)
                            put("performanceNotes", c.performanceNotes); put("isSample", c.isSample)
                        })
                    }
                })
            }
            if (has(AppModule.DOCUMENTS)) {
                data.put("documents", JSONArray().apply {
                    snapshot.documents.forEach { d ->
                        put(JSONObject().apply {
                            put("id", d.id); put("title", d.title); put("category", d.category.name)
                            put("clientId", d.clientId ?: JSONObject.NULL); put("projectId", d.projectId ?: JSONObject.NULL)
                            put("driveUrl", d.driveUrl); put("description", d.description)
                            put("tags", d.tags); put("dateAdded", d.dateAdded); put("isSample", d.isSample)
                        })
                    }
                })
            }
            if (has(AppModule.KNOWLEDGE)) {
                data.put("knowledgeArticles", JSONArray().apply {
                    snapshot.knowledgeArticles.forEach { k ->
                        put(JSONObject().apply {
                            put("id", k.id); put("title", k.title); put("category", k.category.name)
                            put("content", k.content); put("tags", k.tags)
                            put("updatedAt", k.updatedAt); put("isSample", k.isSample)
                        })
                    }
                })
            }
            put("data", data)
        }
        return root.toString(2)
    }

    fun validateAndParse(jsonText: String): ValidationResult {
        return try {
            val root = JSONObject(jsonText)
            val format = root.optString("format", "")
            if (format != FORMAT_IDENTIFIER) {
                return ValidationResult.Invalid("Invalid backup format header")
            }
            val version = root.optInt("schemaVersion", -1)
            if (version != SCHEMA_VERSION) {
                return ValidationResult.Invalid("Incompatible schema version: $version (expected $SCHEMA_VERSION)")
            }
            val data = root.optJSONObject("data")
                ?: return ValidationResult.Invalid("Missing backup 'data' payload")

            val exportedAt = root.optLong("exportedAt", 0L)
            val modulesArr = root.optJSONArray("modules") ?: JSONArray()
            val modules = (0 until modulesArr.length()).map { modulesArr.getString(it) }

            val snapshot = DatabaseSnapshot(
                teamMembers = parseArray(data.optJSONArray("teamMembers")) { o ->
                    UserMemberEntity(
                        id = o.getLong("id"), name = o.getString("name"),
                        email = o.optString("email"), role = UserRole.valueOf(o.getString("role")),
                        department = o.optString("department"), isActive = o.optBoolean("isActive", true),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                },
                clients = parseArray(data.optJSONArray("clients")) { o ->
                    ClientEntity(
                        id = o.getLong("id"), clientCode = o.optString("clientCode"),
                        fullName = o.getString("fullName"), companyName = o.optString("companyName"),
                        email = o.optString("email"), phone = o.optString("phone"),
                        whatsapp = o.optString("whatsapp"), country = o.optString("country", "Iraq"),
                        city = o.optString("city", "Erbil"), address = o.optString("address"),
                        clientType = ClientType.valueOf(o.getString("clientType")),
                        industry = o.optString("industry"), acquisitionSource = o.optString("acquisitionSource"),
                        dateAdded = o.optLong("dateAdded", System.currentTimeMillis()),
                        status = ClientStatus.valueOf(o.getString("status")),
                        notes = o.optString("notes"), tags = o.optString("tags"),
                        isSample = o.optBoolean("isSample", false), isArchived = o.optBoolean("isArchived", false)
                    )
                },
                communications = parseArray(data.optJSONArray("communications")) { o ->
                    ClientCommunicationEntity(
                        id = o.getLong("id"), clientId = o.getLong("clientId"),
                        interactionType = o.getString("interactionType"), summary = o.getString("summary"),
                        requirements = o.optString("requirements"),
                        followUpDate = if (o.isNull("followUpDate")) null else o.getLong("followUpDate"),
                        date = o.optLong("date", System.currentTimeMillis()),
                        isSample = o.optBoolean("isSample", false)
                    )
                },
                projects = parseArray(data.optJSONArray("projects")) { o ->
                    ProjectEntity(
                        id = o.getLong("id"), projectCode = o.optString("projectCode"),
                        name = o.getString("name"),
                        clientId = if (o.isNull("clientId")) null else o.getLong("clientId"),
                        description = o.optString("description"),
                        category = ProjectCategory.valueOf(o.getString("category")),
                        startDate = o.optLong("startDate", System.currentTimeMillis()),
                        deadline = if (o.isNull("deadline")) null else o.getLong("deadline"),
                        budget = o.optDouble("budget", 0.0), agreedPrice = o.optDouble("agreedPrice", 0.0),
                        currency = CurrencyCode.valueOf(o.getString("currency")),
                        status = ProjectStatus.valueOf(o.getString("status")),
                        priority = PriorityLevel.valueOf(o.getString("priority")),
                        assignedMembers = o.optString("assignedMembers"),
                        progressPercent = o.optInt("progressPercent", 0),
                        deliverables = o.optString("deliverables"), externalUrl = o.optString("externalUrl"),
                        classification = o.optString("classification"), internalNotes = o.optString("internalNotes"),
                        isStarter = o.optBoolean("isStarter", false), isSample = o.optBoolean("isSample", false),
                        isArchived = o.optBoolean("isArchived", false),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                    )
                },
                milestones = parseArray(data.optJSONArray("milestones")) { o ->
                    ProjectMilestoneEntity(
                        id = o.getLong("id"), projectId = o.getLong("projectId"),
                        title = o.getString("title"),
                        dueDate = if (o.isNull("dueDate")) null else o.getLong("dueDate"),
                        isCompleted = o.optBoolean("isCompleted", false),
                        isSample = o.optBoolean("isSample", false)
                    )
                },
                tasks = parseArray(data.optJSONArray("tasks")) { o ->
                    TaskEntity(
                        id = o.getLong("id"), title = o.getString("title"),
                        description = o.optString("description"),
                        projectId = if (o.isNull("projectId")) null else o.getLong("projectId"),
                        clientId = if (o.isNull("clientId")) null else o.getLong("clientId"),
                        category = o.optString("category"),
                        priority = PriorityLevel.valueOf(o.getString("priority")),
                        status = TaskStatus.valueOf(o.getString("status")),
                        startDate = o.optLong("startDate", System.currentTimeMillis()),
                        dueDate = if (o.isNull("dueDate")) null else o.getLong("dueDate"),
                        estimatedHours = o.optDouble("estimatedHours", 0.0),
                        actualHours = o.optDouble("actualHours", 0.0),
                        assignedTo = o.optString("assignedTo", "Owner"),
                        notes = o.optString("notes"),
                        completionDate = if (o.isNull("completionDate")) null else o.getLong("completionDate"),
                        isRecurring = o.optBoolean("isRecurring", false),
                        recurrenceLabel = o.optString("recurrenceLabel"),
                        isSample = o.optBoolean("isSample", false)
                    )
                },
                services = parseArray(data.optJSONArray("services")) { o ->
                    ServiceEntity(
                        id = o.getLong("id"), name = o.getString("name"),
                        description = o.optString("description"),
                        category = ServiceCategory.valueOf(o.getString("category")),
                        priceType = ServicePriceType.valueOf(o.getString("priceType")),
                        startingPrice = o.optDouble("startingPrice", 0.0),
                        currency = CurrencyCode.valueOf(o.getString("currency")),
                        estimatedDelivery = o.optString("estimatedDelivery"),
                        requiredInfo = o.optString("requiredInfo"),
                        includedFeatures = o.optString("includedFeatures"),
                        optionalAddOns = o.optString("optionalAddOns"),
                        isActive = o.optBoolean("isActive", true),
                        isSample = o.optBoolean("isSample", false)
                    )
                },
                opportunities = parseArray(data.optJSONArray("opportunities")) { o ->
                    OpportunityEntity(
                        id = o.getLong("id"), title = o.getString("title"),
                        potentialClientName = o.getString("potentialClientName"),
                        clientId = if (o.isNull("clientId")) null else o.getLong("clientId"),
                        requestedServiceId = if (o.isNull("requestedServiceId")) null else o.getLong("requestedServiceId"),
                        requestedServiceName = o.optString("requestedServiceName"),
                        estimatedValue = o.optDouble("estimatedValue", 0.0),
                        currency = CurrencyCode.valueOf(o.getString("currency")),
                        expectedClosingDate = if (o.isNull("expectedClosingDate")) null else o.getLong("expectedClosingDate"),
                        source = o.optString("source"),
                        stage = OpportunityStage.valueOf(o.getString("stage")),
                        probability = o.optInt("probability", 25),
                        notes = o.optString("notes"),
                        nextFollowUpDate = if (o.isNull("nextFollowUpDate")) null else o.getLong("nextFollowUpDate"),
                        communicationNotes = o.optString("communicationNotes"),
                        convertedClientId = if (o.isNull("convertedClientId")) null else o.getLong("convertedClientId"),
                        convertedProjectId = if (o.isNull("convertedProjectId")) null else o.getLong("convertedProjectId"),
                        isSample = o.optBoolean("isSample", false),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                    )
                },
                invoices = parseArray(data.optJSONArray("invoices")) { o ->
                    InvoiceEntity(
                        id = o.getLong("id"), invoiceNumber = o.getString("invoiceNumber"),
                        clientId = if (o.isNull("clientId")) null else o.getLong("clientId"),
                        clientNameSnapshot = o.getString("clientNameSnapshot"),
                        projectId = if (o.isNull("projectId")) null else o.getLong("projectId"),
                        issueDate = o.getLong("issueDate"), dueDate = o.getLong("dueDate"),
                        currency = CurrencyCode.valueOf(o.getString("currency")),
                        subtotal = o.getDouble("subtotal"), discount = o.getDouble("discount"),
                        tax = o.getDouble("tax"), total = o.getDouble("total"),
                        paidAmount = o.getDouble("paidAmount"),
                        status = InvoiceStatus.valueOf(o.getString("status")),
                        notes = o.optString("notes"), isSample = o.optBoolean("isSample", false)
                    )
                },
                invoiceItems = parseArray(data.optJSONArray("invoiceItems")) { o ->
                    InvoiceItemEntity(
                        id = o.getLong("id"), invoiceId = o.getLong("invoiceId"),
                        serviceId = if (o.isNull("serviceId")) null else o.getLong("serviceId"),
                        title = o.getString("title"), quantity = o.getInt("quantity"),
                        unitPrice = o.getDouble("unitPrice"), lineTotal = o.getDouble("lineTotal")
                    )
                },
                payments = parseArray(data.optJSONArray("payments")) { o ->
                    PaymentEntity(
                        id = o.getLong("id"), invoiceId = o.getLong("invoiceId"),
                        amount = o.getDouble("amount"), currency = CurrencyCode.valueOf(o.getString("currency")),
                        paymentDate = o.getLong("paymentDate"), method = PaymentMethod.valueOf(o.getString("method")),
                        notes = o.optString("notes"), isSample = o.optBoolean("isSample", false)
                    )
                },
                transactions = parseArray(data.optJSONArray("transactions")) { o ->
                    TransactionEntity(
                        id = o.getLong("id"), transactionCode = o.optString("transactionCode"),
                        type = TransactionType.valueOf(o.getString("type")),
                        amount = o.getDouble("amount"), currency = CurrencyCode.valueOf(o.getString("currency")),
                        category = TransactionCategory.valueOf(o.getString("category")),
                        date = o.getLong("date"), paymentMethod = PaymentMethod.valueOf(o.getString("paymentMethod")),
                        clientId = if (o.isNull("clientId")) null else o.getLong("clientId"),
                        projectId = if (o.isNull("projectId")) null else o.getLong("projectId"),
                        linkedInvoiceId = if (o.isNull("linkedInvoiceId")) null else o.getLong("linkedInvoiceId"),
                        linkedPaymentId = if (o.isNull("linkedPaymentId")) null else o.getLong("linkedPaymentId"),
                        description = o.optString("description"), receiptUrl = o.optString("receiptUrl"),
                        status = TransactionStatus.valueOf(o.getString("status")),
                        isSample = o.optBoolean("isSample", false),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                    )
                },
                campaigns = parseArray(data.optJSONArray("campaigns")) { o ->
                    CampaignEntity(
                        id = o.getLong("id"), name = o.getString("name"),
                        channel = SocialChannel.valueOf(o.getString("channel")),
                        status = CampaignStatus.valueOf(o.getString("status")),
                        startDate = o.getLong("startDate"),
                        endDate = if (o.isNull("endDate")) null else o.getLong("endDate"),
                        adSpend = o.optDouble("adSpend", 0.0),
                        currency = CurrencyCode.valueOf(o.getString("currency")),
                        leadsGenerated = o.optInt("leadsGenerated", 0),
                        notes = o.optString("notes"), isSample = o.optBoolean("isSample", false)
                    )
                },
                contentItems = parseArray(data.optJSONArray("contentItems")) { o ->
                    ContentItemEntity(
                        id = o.getLong("id"), title = o.getString("title"),
                        platform = SocialChannel.valueOf(o.getString("platform")),
                        contentType = o.optString("contentType"), caption = o.optString("caption"),
                        plannedDate = o.getLong("plannedDate"),
                        status = ContentStatus.valueOf(o.getString("status")),
                        mediaLink = o.optString("mediaLink"),
                        campaignId = if (o.isNull("campaignId")) null else o.getLong("campaignId"),
                        performanceNotes = o.optString("performanceNotes"),
                        isSample = o.optBoolean("isSample", false)
                    )
                },
                documents = parseArray(data.optJSONArray("documents")) { o ->
                    DocumentEntity(
                        id = o.getLong("id"), title = o.getString("title"),
                        category = DocumentCategory.valueOf(o.getString("category")),
                        clientId = if (o.isNull("clientId")) null else o.getLong("clientId"),
                        projectId = if (o.isNull("projectId")) null else o.getLong("projectId"),
                        driveUrl = o.getString("driveUrl"), description = o.optString("description"),
                        tags = o.optString("tags"), dateAdded = o.getLong("dateAdded"),
                        isSample = o.optBoolean("isSample", false)
                    )
                },
                knowledgeArticles = parseArray(data.optJSONArray("knowledgeArticles")) { o ->
                    KnowledgeArticleEntity(
                        id = o.getLong("id"), title = o.getString("title"),
                        category = KnowledgeCategory.valueOf(o.getString("category")),
                        content = o.getString("content"), tags = o.optString("tags"),
                        updatedAt = o.optLong("updatedAt"), isSample = o.optBoolean("isSample", false)
                    )
                }
            )
            ValidationResult.Valid(snapshot = snapshot, exportedAt = exportedAt, includedModules = modules)
        } catch (e: Exception) {
            ValidationResult.Invalid(e.message ?: "Malformed JSON backup")
        }
    }

    private inline fun <T> parseArray(arr: JSONArray?, mapper: (JSONObject) -> T): List<T> {
        if (arr == null) return emptyList()
        return (0 until arr.length()).map { mapper(arr.getJSONObject(it)) }
    }

    fun exportCsv(headers: List<String>, rows: List<List<String>>): String {
        fun escape(cell: String): String {
            val clean = cell.replace("\"", "\"\"")
            return if (clean.contains(",") || clean.contains("\"") || clean.contains("\n")) "\"$clean\"" else clean
        }
        return buildString {
            appendLine(headers.joinToString(",") { escape(it) })
            rows.forEach { row ->
                appendLine(row.joinToString(",") { escape(it) })
            }
        }
    }
}
