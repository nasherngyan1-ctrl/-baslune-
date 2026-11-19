package com.example.data.seed

import com.example.data.local.*
import com.example.domain.model.*

class StarterDataSeeder {

    /**
     * Seeds only the initial owner account, starter service catalog structure, and the 4 known
     * non-revenue BASLUNE starter showcase projects (Section 26).
     * Never fabricates financial totals, client revenue, or fake transactions.
     */
    suspend fun seedStarterFoundation(dao: BasluneDao) {
        if (dao.getAllTeamMembers().isEmpty()) {
            dao.upsertTeamMember(
                UserMemberEntity(
                    name = "BASLUNE Founder / Owner",
                    email = "baslunestudio@gmail.com",
                    role = UserRole.OWNER,
                    department = "Executive & Craft",
                    isActive = true
                )
            )
        }

        if (dao.getAllProjects().isEmpty()) {
            val now = System.currentTimeMillis()
            dao.upsertProject(
                ProjectEntity(
                    projectCode = "BSL-001",
                    name = "BASLUNE Creative Studio Website",
                    description = "Official BASLUNE Studio digital showcase and inquiry hub.",
                    category = ProjectCategory.WEBSITE_DEVELOPMENT,
                    startDate = now,
                    agreedPrice = 0.0,
                    budget = 0.0,
                    currency = CurrencyCode.USD,
                    status = ProjectStatus.COMPLETED,
                    priority = PriorityLevel.HIGH,
                    progressPercent = 100,
                    externalUrl = "https://baslune-creative-studio.ai.studio/",
                    classification = "Studio Flagship Website (Non-Commercial Starter Record)",
                    isStarter = true,
                    isSample = false
                )
            )
            dao.upsertProject(
                ProjectEntity(
                    projectCode = "BSL-002",
                    name = "BASLUNE Specialty Coffee Website",
                    description = "Luxury specialty coffee brand & menu web showcase.",
                    category = ProjectCategory.WEBSITE_DEVELOPMENT,
                    startDate = now,
                    agreedPrice = 0.0,
                    budget = 0.0,
                    currency = CurrencyCode.USD,
                    status = ProjectStatus.COMPLETED,
                    priority = PriorityLevel.NORMAL,
                    progressPercent = 100,
                    externalUrl = "https://baslune-specialty-coffee.ai.studio/",
                    classification = "Demonstration website, not an actual coffee shop",
                    isStarter = true,
                    isSample = false
                )
            )
            dao.upsertProject(
                ProjectEntity(
                    projectCode = "BSL-003",
                    name = "BASLUNE POS",
                    description = "Modern retail & cafe point-of-sale interface concept.",
                    category = ProjectCategory.WEB_APPLICATIONS,
                    startDate = now,
                    agreedPrice = 0.0,
                    budget = 0.0,
                    currency = CurrencyCode.IQD,
                    status = ProjectStatus.UNDER_REVIEW,
                    priority = PriorityLevel.NORMAL,
                    progressPercent = 85,
                    classification = "Demonstration point-of-sale project",
                    isStarter = true,
                    isSample = false
                )
            )
            dao.upsertProject(
                ProjectEntity(
                    projectCode = "BSL-004",
                    name = "Erbil Delivery Manager",
                    description = "Delivery-driver dispatch and fleet workflow PWA concept tailored for Erbil.",
                    category = ProjectCategory.BUSINESS_AUTOMATION,
                    startDate = now,
                    agreedPrice = 0.0,
                    budget = 0.0,
                    currency = CurrencyCode.IQD,
                    status = ProjectStatus.PLANNING,
                    priority = PriorityLevel.HIGH,
                    progressPercent = 40,
                    classification = "Delivery-driver management PWA concept for Erbil",
                    isStarter = true,
                    isSample = false
                )
            )
        }
    }

    /**
     * Optional explicitly activated sample records clearly tagged isSample = true.
     * Can be removed cleanly at any time without touching real studio records.
     */
    suspend fun seedSampleBusinessData(dao: BasluneDao) {
        val now = System.currentTimeMillis()
        val day = 24L * 60 * 60 * 1000

        // Sample Services
        val webSvcId = dao.upsertService(
            ServiceEntity(
                name = "Custom Business Website (Multilingual)",
                description = "Sorani, Arabic, and English responsive website with luxury editorial design.",
                category = ServiceCategory.WEBSITE_DESIGN_DEVELOPMENT,
                priceType = ServicePriceType.FIXED,
                startingPrice = 1200.0,
                currency = CurrencyCode.USD,
                estimatedDelivery = "2 - 3 Weeks",
                requiredInfo = "Brand logo, business overview, photos, contact channels",
                includedFeatures = "RTL/LTR support, SEO setup, WhatsApp integration",
                optionalAddOns = "Online booking, custom admin dashboard",
                isActive = true,
                isSample = true
            )
        )
        val brandSvcId = dao.upsertService(
            ServiceEntity(
                name = "Brand Identity & Visual System",
                description = "Logo suite, typography, color palette, and social media brand kit.",
                category = ServiceCategory.BRANDING_IDENTITY,
                priceType = ServicePriceType.PACKAGE,
                startingPrice = 950000.0,
                currency = CurrencyCode.IQD,
                estimatedDelivery = "10 - 14 Days",
                requiredInfo = "Target audience, brand values, competitor references",
                includedFeatures = "Primary & secondary logos, brand book PDF, social templates",
                optionalAddOns = "Packaging design, print collateral",
                isActive = true,
                isSample = true
            )
        )
        dao.upsertService(
            ServiceEntity(
                name = "AI Customer Support Chatbot",
                description = "Automated Kurdish/Arabic/English WhatsApp & Web assistant for businesses.",
                category = ServiceCategory.AI_CHATBOTS,
                priceType = ServicePriceType.CUSTOM_QUOTATION,
                startingPrice = 850.0,
                currency = CurrencyCode.USD,
                estimatedDelivery = "2 Weeks",
                requiredInfo = "FAQ sheet, product catalog, escalation rules",
                includedFeatures = "24/7 automated replies, lead capture sheet",
                optionalAddOns = "CRM synchronization",
                isActive = true,
                isSample = true
            )
        )

        // Sample Clients
        val c1 = dao.upsertClient(
            ClientEntity(
                clientCode = "SMP-101",
                fullName = "Zanko Architecture Group",
                companyName = "Zanko Design & Build",
                email = "info@zanko-sample.example",
                phone = "0750 000 0001",
                whatsapp = "0750 000 0001",
                country = "Iraq",
                city = "Erbil",
                address = "Empire World, Erbil",
                clientType = ClientType.COMPANY,
                industry = "Architecture & Real Estate",
                acquisitionSource = "Instagram",
                dateAdded = now - 25 * day,
                status = ClientStatus.ACTIVE,
                notes = "Sample client record for previewing CRM and financial links.",
                tags = "Erbil, Architecture, VIP",
                isSample = true
            )
        )
        val c2 = dao.upsertClient(
            ClientEntity(
                clientCode = "SMP-102",
                fullName = "Nishtiman Roastery",
                companyName = "Nishtiman Trading",
                email = "orders@nishtiman-sample.example",
                phone = "0750 000 0002",
                whatsapp = "0750 000 0002",
                country = "Iraq",
                city = "Sulaymaniyah",
                clientType = ClientType.BUSINESS,
                industry = "Food & Beverage",
                acquisitionSource = "Referral",
                dateAdded = now - 10 * day,
                status = ClientStatus.NEGOTIATING,
                notes = "Interested in branding refresh + e-commerce storefront.",
                tags = "Branding, E-Commerce",
                isSample = true
            )
        )

        dao.upsertCommunication(
            ClientCommunicationEntity(
                clientId = c1,
                interactionType = "Discovery Meeting",
                summary = "Reviewed bilingual Sorani/English sitemap and architectural gallery needs.",
                requirements = "Project showcase filterable by residential and commercial.",
                followUpDate = now + 2 * day,
                date = now - 12 * day,
                isSample = true
            )
        )

        // Sample Projects
        val p1 = dao.upsertProject(
            ProjectEntity(
                projectCode = "SMP-PRJ-1",
                name = "Zanko Corporate Web Portal",
                clientId = c1,
                description = "Luxury bilingual architecture portfolio and consultation booking system.",
                category = ProjectCategory.WEBSITE_DEVELOPMENT,
                startDate = now - 20 * day,
                deadline = now + 5 * day,
                budget = 2500.0,
                agreedPrice = 2400.0,
                currency = CurrencyCode.USD,
                status = ProjectStatus.IN_PROGRESS,
                priority = PriorityLevel.HIGH,
                progressPercent = 65,
                deliverables = "UI Design, Responsive Web App, CMS",
                internalNotes = "Sample commercial project.",
                isSample = true
            )
        )
        val p2 = dao.upsertProject(
            ProjectEntity(
                projectCode = "SMP-PRJ-2",
                name = "Nishtiman Visual Identity",
                clientId = c2,
                description = "Complete visual identity & packaging system.",
                category = ProjectCategory.BRANDING_GRAPHIC_DESIGN,
                startDate = now - 8 * day,
                deadline = now + 12 * day,
                budget = 1200000.0,
                agreedPrice = 1200000.0,
                currency = CurrencyCode.IQD,
                status = ProjectStatus.PLANNING,
                priority = PriorityLevel.NORMAL,
                progressPercent = 30,
                isSample = true
            )
        )

        dao.upsertMilestone(
            ProjectMilestoneEntity(
                projectId = p1,
                title = "Wireframes & Design Direction Approved",
                dueDate = now - 10 * day,
                isCompleted = true,
                isSample = true
            )
        )
        dao.upsertMilestone(
            ProjectMilestoneEntity(
                projectId = p1,
                title = "Production Deployment & Handover",
                dueDate = now + 5 * day,
                isCompleted = false,
                isSample = true
            )
        )

        // Sample Tasks
        dao.upsertTask(
            TaskEntity(
                title = "Finalize Zanko mobile navigation RTL polish",
                projectId = p1,
                clientId = c1,
                category = "Frontend",
                priority = PriorityLevel.HIGH,
                status = TaskStatus.IN_PROGRESS,
                startDate = now - 2 * day,
                dueDate = now + 1 * day,
                estimatedHours = 4.0,
                assignedTo = "BASLUNE Founder / Owner",
                isSample = true
            )
        )
        dao.upsertTask(
            TaskEntity(
                title = "Send updated typography options to Nishtiman",
                projectId = p2,
                clientId = c2,
                category = "Design",
                priority = PriorityLevel.URGENT,
                status = TaskStatus.NOT_STARTED,
                startDate = now - 4 * day,
                dueDate = now - 1 * day, // Intentionally overdue for testing alerts!
                estimatedHours = 2.0,
                assignedTo = "BASLUNE Founder / Owner",
                isSample = true
            )
        )

        // Sample Opportunity
        dao.upsertOpportunity(
            OpportunityEntity(
                title = "Hawler Medical Clinic AI Assistant",
                potentialClientName = "Hawler Care Clinic",
                requestedServiceId = webSvcId,
                requestedServiceName = "Custom Business Website + AI Chatbot",
                estimatedValue = 1800.0,
                currency = CurrencyCode.USD,
                expectedClosingDate = now + 7 * day,
                source = "TikTok",
                stage = OpportunityStage.PROPOSAL_SENT,
                probability = 70,
                notes = "Proposal sent; awaiting clinic director review.",
                nextFollowUpDate = now + 2 * day,
                isSample = true
            )
        )

        // Sample Invoices & Payments & Transactions (cleanly linked without double counting)
        val inv1 = dao.upsertInvoice(
            InvoiceEntity(
                invoiceNumber = "SMP-INV-001",
                clientId = c1,
                clientNameSnapshot = "Zanko Architecture Group",
                projectId = p1,
                issueDate = now - 15 * day,
                dueDate = now - 2 * day,
                currency = CurrencyCode.USD,
                subtotal = 2400.0,
                discount = 0.0,
                tax = 0.0,
                total = 2400.0,
                paidAmount = 1200.0,
                status = InvoiceStatus.PARTIALLY_PAID,
                notes = "50% advance payment received; remaining 50% on launch.",
                isSample = true
            )
        )
        dao.upsertInvoiceItem(
            InvoiceItemEntity(
                invoiceId = inv1,
                serviceId = webSvcId,
                title = "Custom Business Website (Multilingual)",
                quantity = 1,
                unitPrice = 2400.0,
                lineTotal = 2400.0
            )
        )
        val pay1 = dao.upsertPayment(
            PaymentEntity(
                invoiceId = inv1,
                amount = 1200.0,
                currency = CurrencyCode.USD,
                paymentDate = now - 14 * day,
                method = PaymentMethod.FIB,
                notes = "50% project kickoff deposit",
                isSample = true
            )
        )
        dao.upsertTransaction(
            TransactionEntity(
                transactionCode = "SMP-TX-001",
                type = TransactionType.INCOME,
                amount = 1200.0,
                currency = CurrencyCode.USD,
                category = TransactionCategory.INCOME_WEBSITE_DEVELOPMENT,
                date = now - 14 * day,
                paymentMethod = PaymentMethod.FIB,
                clientId = c1,
                projectId = p1,
                linkedInvoiceId = inv1,
                linkedPaymentId = pay1,
                description = "Invoice SMP-INV-001 deposit",
                status = TransactionStatus.COMPLETED,
                isSample = true
            )
        )
        dao.upsertTransaction(
            TransactionEntity(
                transactionCode = "SMP-TX-002",
                type = TransactionType.EXPENSE,
                amount = 45000.0,
                currency = CurrencyCode.IQD,
                category = TransactionCategory.EXPENSE_INTERNET,
                date = now - 5 * day,
                paymentMethod = PaymentMethod.ZAIN_CASH,
                description = "Studio fiber internet monthly bill",
                status = TransactionStatus.COMPLETED,
                isSample = true
            )
        )
        dao.upsertTransaction(
            TransactionEntity(
                transactionCode = "SMP-TX-003",
                type = TransactionType.INCOME,
                amount = 600000.0,
                currency = CurrencyCode.IQD,
                category = TransactionCategory.INCOME_BRANDING,
                date = now - 6 * day,
                paymentMethod = PaymentMethod.CASH,
                clientId = c2,
                projectId = p2,
                description = "Nishtiman Branding Initial Advance",
                status = TransactionStatus.COMPLETED,
                isSample = true
            )
        )

        // Sample Marketing & Knowledge
        val campId = dao.upsertCampaign(
            CampaignEntity(
                name = "Erbil Digital Craft Autumn Campaign",
                channel = SocialChannel.INSTAGRAM,
                status = CampaignStatus.ACTIVE,
                startDate = now - 12 * day,
                endDate = now + 18 * day,
                adSpend = 150.0,
                currency = CurrencyCode.USD,
                leadsGenerated = 9,
                notes = "Targeting local cafes, clinics, and architectural firms in Erbil.",
                isSample = true
            )
        )
        dao.upsertContentItem(
            ContentItemEntity(
                title = "Behind the Scenes: Designing Bilingual RTL Apps",
                platform = SocialChannel.TIKTOK,
                contentType = "Short Reel",
                caption = "How BASLUNE crafts refined Kurdish & Arabic digital products.",
                plannedDate = now + 2 * day,
                status = ContentStatus.SCHEDULED,
                campaignId = campId,
                isSample = true
            )
        )
        dao.upsertKnowledgeArticle(
            KnowledgeArticleEntity(
                title = "BASLUNE Client Onboarding Checklist",
                category = KnowledgeCategory.PROJECT_CHECKLISTS,
                content = "1. Confirm scope & currency (IQD/USD).\n2. Issue 50% advance invoice.\n3. Collect brand assets into shared Google Drive folder.\n4. Set milestone dates in BASLUNE OS.",
                tags = "Onboarding, SOP, Checklist",
                isSample = true
            )
        )
    }
}
