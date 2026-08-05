const { AppError } = require('../middleware/errors');
const { validateContactInput, validateFields } = require('../utils/validation');

const CONTACT_FIELDS=new Set(['contact_name','contact_role','phone','email','availability','display_order','status']);

function createContactService(db, auditService) {
  const find = db.prepare('SELECT * FROM emergency_contacts WHERE id=?');
  function validate(payload) { const result=validateContactInput(payload); if(!result.valid) throw new AppError(400,result.errors[0],result.errors); return result.value; }
  function list({ isAdmin=false }={}) { return db.prepare(`SELECT * FROM emergency_contacts ${isAdmin?'':"WHERE status='ACTIVE'"} ORDER BY display_order,id`).all(); }
  function getById(id,{isAdmin=false}={}) { const row=find.get(id); if(!row||(!isAdmin&&row.status!=='ACTIVE')) throw new AppError(404,'Emergency contact not found.'); return row; }
  function create(payload,context){const value=validate(payload);return db.transaction(()=>{const info=db.prepare(`INSERT INTO emergency_contacts(contact_name,contact_role,phone,email,availability,display_order,status)
    VALUES(@contact_name,@contact_role,@phone,@email,@availability,@display_order,@status)`).run(value);const id=Number(info.lastInsertRowid);auditService.record({actorUserId:context.actorUserId,action:'CONTACT_CREATE',entityType:'EMERGENCY_CONTACT',entityId:id,outcome:'SUCCESS',requestId:context.requestId,metadata:{changed_fields:Object.keys(value)}});return getById(id,{isAdmin:true});})();}
  function update(id,payload,context){const existing=find.get(id);if(!existing)throw new AppError(404,'Emergency contact not found.');const fieldErrors=validateFields(payload,CONTACT_FIELDS);if(fieldErrors.length)throw new AppError(400,fieldErrors[0],fieldErrors);const candidate=Object.fromEntries([...CONTACT_FIELDS].map((field)=>[field,Object.prototype.hasOwnProperty.call(payload,field)?payload[field]:existing[field]]));const value=validate(candidate);const action=existing.status!=='ACTIVE'&&value.status==='ACTIVE'?'CONTACT_REACTIVATE':existing.status==='ACTIVE'&&value.status!=='ACTIVE'?'CONTACT_DEACTIVATE':'CONTACT_UPDATE';return db.transaction(()=>{db.prepare(`UPDATE emergency_contacts SET contact_name=@contact_name,contact_role=@contact_role,phone=@phone,email=@email,availability=@availability,display_order=@display_order,status=@status,updated_at=CURRENT_TIMESTAMP WHERE id=@id`).run({...value,id});auditService.record({actorUserId:context.actorUserId,action,entityType:'EMERGENCY_CONTACT',entityId:id,outcome:'SUCCESS',requestId:context.requestId,metadata:{changed_fields:Object.keys(payload),previous_status:existing.status,new_status:value.status}});return getById(id,{isAdmin:true});})();}
  return{list,getById,create,update,deactivate:(id,c)=>update(id,{status:'INACTIVE'},c),reactivate:(id,c)=>update(id,{status:'ACTIVE'},c)};
}
module.exports={createContactService};
