import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.PropertyDetailsView

class PropertyDetailsController @Inject()(
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    
)